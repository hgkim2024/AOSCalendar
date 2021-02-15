package com.asusoft.calendar.fragment.month

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.ActivityAddEvent
import com.asusoft.calendar.activity.ActivityStart
import com.asusoft.calendar.fragment.month.objects.MonthItem
import com.asusoft.calendar.fragment.month.objects.WeekItem
import com.asusoft.calendar.realm.RealmEventMultiDay
import com.asusoft.calendar.realm.RealmEventOneDay
import com.asusoft.calendar.util.`object`.CalculatorUtil
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil.EVENT_HEIGHT
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.getBoundsLocation
import com.asusoft.calendar.util.extension.removeFromSuperView
import com.asusoft.calendar.util.getNextDay
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.event.OneDayEventHolder
import com.asusoft.calendar.util.startOfMonth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FragmentMonthPage: Fragment() {

    lateinit var date: Date
    lateinit var monthItem: MonthItem
    lateinit var page: View

    var prevClickDayView: View? = null
    var prevDayEventView: ConstraintLayout? = null
    var monthCalendar: ConstraintLayout? = null
    var initFlag = false
    var bottomFlag = false
    var dialogHeight = 0

    companion object {
        fun newInstance(time: Long, initFlag: Boolean): FragmentMonthPage {
            val f = FragmentMonthPage()
            val args = Bundle()
            args.putLong("time", time)
            args.putBoolean("initFlag", initFlag)
            f.arguments = args
            return f
        }
        const val ANIMATION_DURATION: Long = 150
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments!!
        val time = args.getLong("time")
        initFlag = args.getBoolean("initFlag", false)

        date = Date(time)

        GlobalBus.getBus().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        GlobalBus.getBus().unregister(this)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = this.context!!
        val inflater = LayoutInflater.from(context)
        page = inflater.inflate(R.layout.fragment_month_calender, null, false)

        if (initFlag) {
            setPageUI(context)
        }

        return page
    }

    override fun onResume() {
        super.onResume()

        setActionBarTitle()
        setAsyncPageUI(context!!)

        if (prevClickDayView != null) {
            for (weekItem in monthItem.WeekItemList) {
                for (idx in weekItem.dayViewList.indices) {
                    val dayView = weekItem.dayViewList[idx]
                    if (prevClickDayView == dayView) {
                        postSelectedDayDate(weekItem.weekDate.getNextDay(idx))
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val addEventActivity = event.map.getOrDefault(ActivityAddEvent.toStringActivity(), null)
        if (addEventActivity != null) {
            removeDayEventView()
            refreshPage()
        }

        val monthViewPager = event.map.getOrDefault(FragmentMonthViewPager.toString(), null)
        if (monthViewPager != null) {
            setAsyncPageUI(context!!)
        }
        
        val oneDayEventHolder = event.map.getOrDefault(OneDayEventHolder.toString(), null)
        if (oneDayEventHolder != null) {

            val date = event.map.getOrDefault("date", null) as? Date ?: return
            if (date != this.date.startOfMonth) return

            val key = event.map.getOrDefault("key", null) as? Long
            if (key != null) {
                
                val oneDayItem = RealmEventOneDay.select(key)
                if (oneDayItem != null) {
                    val intent = Intent(context, ActivityAddEvent::class.java)
                    intent.putExtra("startDate", Date(oneDayItem.time))
                    intent.putExtra("endDate", Date(oneDayItem.time))
                    intent.putExtra("title", oneDayItem.name)
                    intent.putExtra("isEdit", true)
                    intent.putExtra("key", key)
                    startActivity(intent)
                    return
                }

                val multiDayItem = RealmEventMultiDay.select(key)
                if (multiDayItem != null) {
                    val intent = Intent(context, ActivityAddEvent::class.java)
                    intent.putExtra("startDate", Date(multiDayItem.startTime))
                    intent.putExtra("endDate", Date(multiDayItem.endTime))
                    intent.putExtra("title", multiDayItem.name)
                    intent.putExtra("isEdit", true)
                    intent.putExtra("key", key)
                    startActivity(intent)
                    return
                }
            }
        }
    }

    private fun setAsyncPageUI(context: Context) {
        GlobalScope.async {
            page.post {
                setPageUI(context)
            }
        }
    }

    private fun setPageUI(context: Context) {
        monthCalendar = page.findViewById(R.id.month_calendar)
        if (monthCalendar?.childCount == 0) {
            monthItem = MonthCalendarUIUtil.getMonthUI(context, date.startOfMonth)
            monthCalendar?.addView(monthItem.monthView)
        }

        for (weekItem in monthItem.WeekItemList) {
            for (idx in weekItem.dayViewList.indices) {
                val dayView = weekItem.dayViewList[idx]


                dayView.setOnClickListener {
                    if (prevClickDayView != null) {
                        prevClickDayView!!.setBackgroundColor(ContextCompat.getColor(context, R.color.background))
                    }

                    if (prevDayEventView != null) {
                        removeDayEventView(
                                context,
                                weekItem,
                                dayView,
                                idx
                        )
                    } else {
                        showOneDayEventView(
                                context,
                                weekItem,
                                dayView,
                                idx
                        )
                    }

                }


            }
        }
    }

    private fun setOneDayEventView(
            dayView: View,
            date: Date,
            point: Point
    ) {
        if (monthCalendar == null) return
        val monthCalendar = monthCalendar!!

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.view_one_day_pop_up, null, false)
        val eventLayout = view.findViewById<ConstraintLayout>(R.id.root_layout)
        val title = view.findViewById<TextView>(R.id.title)
        val emptyTitle = view.findViewById<TextView>(R.id.tv_empty)
        val addButton = view.findViewById<ImageButton>(R.id.add_button)

        monthCalendar.addView(eventLayout)
        prevDayEventView = eventLayout

        val eventList = getEventList(date)

        title.text = "${eventList.size}개 이벤트"

        val addEventListener = View.OnClickListener {
            postSelectedDayDate(date, true)
        }

        addButton.setOnClickListener(addEventListener)

        if (eventList.isEmpty()) {
            emptyTitle.visibility = View.VISIBLE
            emptyTitle.isClickable = true

            emptyTitle.setOnClickListener(addEventListener)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = RecyclerViewAdapter(this, eventList)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        var dialogWidth: Int = 150
        dialogHeight = 30 + 14 + (EVENT_HEIGHT.toInt() * eventList.size)
        if (eventList.isEmpty()) dialogHeight += EVENT_HEIGHT.toInt()

        dialogWidth = CalculatorUtil.dpToPx(dialogWidth.toFloat())
        dialogHeight = CalculatorUtil.dpToPx(dialogHeight.toFloat())

        if (point.y + dayView.height + CalculatorUtil.dpToPx(1.0F) < monthCalendar.height) {
            if (point.y + dayView.height + dialogHeight >= monthCalendar.height - 10) {
                dialogHeight = monthCalendar.height - point.y - dayView.height - 10
            }
        } else {
            if (dialogHeight + dayView.height >= monthCalendar.height - 10) {
                dialogHeight = monthCalendar.height - dayView.height - 10
            }
        }

        eventLayout.layoutParams = ConstraintLayout.LayoutParams(
                dialogWidth,
                dialogHeight
        )

        Log.d("Asu", "Click Point: $point")

        val set = ConstraintSet()
        set.clone(monthCalendar)

        val topMargin =
                if (point.y + dayView.height + dialogHeight >= monthCalendar.height) {
                    bottomFlag = true
                    point.y - dialogHeight
                }
                else {
                    bottomFlag = false
                    point.y + dayView.height
                }

        val startMargin =
                if (point.x + dialogWidth >= monthCalendar.width)
                    point.x + dayView.width - dialogWidth
                else
                    point.x

        set.connect(eventLayout.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, topMargin)
        set.connect(eventLayout.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, startMargin)

        set.applyTo(monthCalendar)

        val animationSet = AnimationSet(false)

        val scaleAnim = ScaleAnimation(1F, 1F, 0F, 1F)
        animationSet.addAnimation(scaleAnim)

        val translateAnim = TranslateAnimation(0F, 0F, if (bottomFlag) dialogHeight.toFloat() else 0F, 0F)
        animationSet.addAnimation(translateAnim)

        animationSet.duration = ANIMATION_DURATION
        eventLayout.startAnimation(animationSet)
    }

    private fun removeDayEventView(
            context: Context,
            weekItem: WeekItem,
            dayView: View,
            idx: Int
    ) {
        if (prevDayEventView != null) {
            val view = prevDayEventView!!
            prevDayEventView = null

            val animationSet = AnimationSet(false)

            val scaleAnim = ScaleAnimation(1F, 1F, 1F, 0F)
            animationSet.addAnimation(scaleAnim)

            val translateAnim = TranslateAnimation(0F, 0F, 0F, if (bottomFlag) dialogHeight.toFloat() else 0F)
            animationSet.addAnimation(translateAnim)

            animationSet.duration = ANIMATION_DURATION

            animationSet.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    view.removeFromSuperView()

                    if (prevClickDayView == dayView) return

                    showOneDayEventView(
                            context,
                            weekItem,
                            dayView,
                            idx
                    )
                }
            })

            view.startAnimation(animationSet)
        }
    }

    private fun removeDayEventView() {
        if (prevDayEventView != null) {
            prevDayEventView!!.removeFromSuperView()
            prevDayEventView = null
        }
    }

    private fun showOneDayEventView(
            context: Context,
            weekItem: WeekItem,
            dayView: View,
            idx: Int
    ) {
//        dayView.background = ContextCompat.getDrawable(context, R.drawable.border)
        dayView.setBackgroundColor(ContextCompat.getColor(context, R.color.separator))
        prevClickDayView = dayView

        postSelectedDayDate(weekItem.weekDate.getNextDay(idx))

        val xPoint = dayView.getBoundsLocation()
        val yPoint = weekItem.rootLayout.getBoundsLocation()
        setOneDayEventView(
                dayView,
                weekItem.weekDate.getNextDay(idx),
                Point(xPoint.x, yPoint.y)
        )
    }

    private fun getEventList(date: Date): ArrayList<Any> {
        val eventList: ArrayList<Any> = ArrayList<Any>()
        val oneDayCopyList = RealmEventOneDay.getOneDayCopyList(date)
        val multiDayCopyList = RealmEventMultiDay.getOneDayCopyList(date)
        val orderMap = MonthCalendarUIUtil.getEventOrderList(date)

        var order = 0
        while (
                !(oneDayCopyList.isEmpty()
                        && multiDayCopyList.isEmpty())
        ) {
            for (item in orderMap) {
                if (order == item.value) {
                    val dayFilter = oneDayCopyList.filter { it.key == item.key }
                    if (dayFilter.isNotEmpty()) {
                        val filterItem = dayFilter.first()
                        eventList.add(filterItem)
                        oneDayCopyList.remove(filterItem)
                        break
                    }

                    val multiDayFilter = multiDayCopyList.filter { it.key == item.key }
                    if (multiDayFilter.isNotEmpty()) {
                        val filterItem = multiDayFilter.first()
                        eventList.add(filterItem)
                        multiDayCopyList.remove(filterItem)
                        break
                    }
                }
            }
            order++
        }

        return eventList
    }

    private fun postSelectedDayDate(date: Date, isAdd: Boolean = false) {
        val event = HashMapEvent(HashMap())
        event.map[FragmentMonthPage.toString()] = FragmentMonthPage.toString()
        event.map["date"] = date
        if (isAdd) {
            event.map["add"] = true
        }
        GlobalBus.getBus().post(event)
    }

    private fun refreshPage() {
        removePage()
        setAsyncPageUI(context!!)
    }

    private fun removePage() {
        monthCalendar?.removeAllViews()
    }

    private fun setActionBarTitle() {
        val sdf = SimpleDateFormat("yyyy년 MM월")
        val title = sdf.format(date)

        (activity as ActivityStart).setTitle(title)
//        Log.d("Asu", "onResume date: $title")
    }
}