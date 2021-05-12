package com.asusoft.calendar.activity.calendar.fragment.week

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.addEvent.activity.ActivityAddEvent
import com.asusoft.calendar.activity.calendar.activity.ActivityCalendar
import com.asusoft.calendar.activity.calendar.fragment.month.MonthCalendarUiUtil
import com.asusoft.calendar.activity.calendar.fragment.week.objects.WeekItem
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.realm.copy.CopyEventDay
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.getBoundsLocation
import com.asusoft.calendar.util.extension.removeFromSuperView
import com.asusoft.calendar.util.objects.CalculatorUtil
import com.asusoft.calendar.util.objects.CalendarUtil
import com.asusoft.calendar.util.recyclerview.RecyclerItemClickListener
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.helper.ItemTouchHelperCallback
import com.asusoft.calendar.util.recyclerview.holder.calendar.eventpopup.OneDayEventHolder
import com.jakewharton.rxbinding4.view.clicks
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.TimeUnit

class FragmentWeekPage: Fragment() {

    companion object {
        fun newInstance(time: Long, initFlag: Boolean): FragmentWeekPage {
            val f = FragmentWeekPage()

            val args = Bundle()
            args.putLong("time", time)
            args.putBoolean("initFlag", initFlag)

            f.arguments = args
            return f
        }

        const val ANIMATION_DURATION: Long = 150
        var dragInitFlag = true
    }

    private lateinit var date: Date
    private var initFlag = false

    private var weekItem: WeekItem? = null
    private lateinit var page: View
    private var weekCalendar: ConstraintLayout? = null

    lateinit var eventViewDate: Date
    private var prevClickDayView: View? = null
    private var prevDayEventView: ConstraintLayout? = null
    private var preventDoubleClickFlag = true
    private var dialogHeight = 0
    private var bottomFlag = false

    private var dragStartDay = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments!!

        val time = args.getLong("time")
        initFlag = args.getBoolean("initFlag", false)

        date = Date(time)
        GlobalBus.register(this)
//        Logger.d("onCreate date: ${date.startOfWeek.toStringDay()}")
//        Logger.d("register week date: ${date.toStringDay()}, address: $this")
    }

    override fun onDestroy() {
        super.onDestroy()

        GlobalBus.unregister(this)
//        Logger.d("unregister week date: ${date.toStringDay()}, address: $this")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = this.context!!

        page = inflater.inflate(R.layout.fragment_constraint_layout, null, false)

        if (initFlag) {
            setPageUI(context)
        }

        return page
    }

    override fun onResume() {
        super.onResume()
        val context = context!!

        setActionBarTitle()
        setAsyncPageUI(context)
        updateHeaderText()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setPageUI(context: Context) {
        weekCalendar = page.findViewById(R.id.calendar)
        if (weekItem == null) {
//            Logger.d("setPageUI date: ${date.toStringDay()}")
            weekItem = WeekCalendarUiUtil.getOneWeekUI(context, date.startOfWeek)
            val weekItem = weekItem!!
            weekCalendar?.addView(weekItem.rootLayout)

//            Logger.d("weekItem!!.eventViewList: ${weekItem.eventViewList.size}")

            val tvEmpty = weekItem.weekLayout.findViewWithTag<TextView>("tv_empty")
            if (WeekCalendarUiUtil.isEmptyEvent(weekItem)) {
                tvEmpty.visibility = View.VISIBLE
            } else {
                tvEmpty.visibility = View.GONE
            }

            for (idx in weekItem.dayViewList.indices) {
                val dayView = weekItem.dayViewList[idx]
                dayView.setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> { }
                        MotionEvent.ACTION_MOVE -> { }
                        MotionEvent.ACTION_UP -> {
                            dayViewClick(
                                    weekItem,
                                    dayView,
                                    idx,
                                    event.x.toInt()
                            )
                        }
                    }
                    true
                }
            }
        }
    }

    private fun setAsyncPageUI(context: Context) {
        GlobalScope.async(Dispatchers.Main) {
            setPageUI(context)
        }
    }

    private fun updateHeaderText() {
        val event = HashMapEvent(HashMap())
        event.map[FragmentWeekPage.toString()] = FragmentWeekPage.toString()
        event.map["date"] = date
        GlobalBus.post(event)
    }

    private fun setActionBarTitle() {
        if (activity is ActivityCalendar) {
            (activity as ActivityCalendar).setTitle(date.toStringMonth())
        }
    }

    fun refreshPage() {
        if (weekItem == null) return
        if (weekCalendar == null) return

        WeekCalendarUiUtil.refreshPage(context!!, weekItem!!, prevDayEventView)
    }

    private fun dayViewClick(
            weekItem: WeekItem,
            dayView: View,
            idx: Int,
            xPosition: Int
    ) {
        if (!preventDoubleClickFlag) return
        preventDoubleClickFlag = false

        if (prevClickDayView != null) {
            prevClickDayView!!.setBackgroundColor(CalendarApplication.getColor(R.color.background))
        }

        if (prevDayEventView != null) {
            removeDayEventView(
                    weekItem,
                    dayView,
                    idx,
                    xPosition
            )
        } else {
            showOneDayEventView(
                    weekItem,
                    dayView,
                    idx,
                    xPosition
            )
        }

        GlobalScope.async {
            delay(ANIMATION_DURATION + 50L)
            preventDoubleClickFlag = true
        }
    }

    private fun removeDayEventView(
            weekItem: WeekItem,
            dayView: View,
            idx: Int,
            xPosition: Int
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
                    view.visibility = View.GONE
                    GlobalScope.async (Dispatchers.Main) {
                        delay(ANIMATION_DURATION)
                        view.removeFromSuperView()
                    }

                    if (prevClickDayView == dayView) return

                    showOneDayEventView(
                            weekItem,
                            dayView,
                            idx,
                            xPosition
                    )
                }
            })

            view.startAnimation(animationSet)
        }
    }

    private fun removeDayEventView() {
        if (prevClickDayView != null) {
            prevClickDayView!!.setBackgroundColor(CalendarApplication.getColor(R.color.background))
            prevClickDayView = null
        }

        if (prevDayEventView != null) {
            prevDayEventView!!.removeFromSuperView()
            prevDayEventView = null
        }
    }

    private fun showOneDayEventView(
            weekItem: WeekItem,
            dayView: View,
            idx: Int,
            xPosition: Int
    ) {
        dayView.setBackgroundColor(CalendarApplication.getColor(R.color.separator))
        prevClickDayView = dayView

        val selectedDate = weekItem.weekDate.getNextDay(idx)
        eventViewDate = selectedDate

        val yPoint = weekItem.dayViewList[idx].getBoundsLocation()

        setOneDayEventView(
                dayView,
                weekItem.weekDate.getNextDay(idx),
                Point(xPosition, yPoint.y)
        )
    }

    private fun setOneDayEventView(
            dayView: View,
            date: Date,
            point: Point
    ) {
        if (weekItem == null) return
        val weekCalendar = weekItem!!.weekLayout

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.view_one_day_pop_up, null, false)
        val eventLayout = view.findViewById<ConstraintLayout>(R.id.root_layout)
        val title = view.findViewById<TextView>(R.id.title)
        val emptyTitle = view.findViewById<TextView>(R.id.tv_empty)
        val addButton = view.findViewById<ImageButton>(R.id.add_button)

        weekCalendar.addView(eventLayout)
        prevDayEventView = eventLayout

        val eventList = CalendarUtil.getDayEventList(date)

        title.text = "${eventList.size}개 이벤트"
        title.textSize = MonthCalendarUiUtil.FONT_SIZE + 4

        addButton.clicks()
                .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    selectedDayDate(date)
                }

        if (eventList.isEmpty()) {
            emptyTitle.visibility = View.VISIBLE
            emptyTitle.textSize = MonthCalendarUiUtil.FONT_SIZE + 3
            emptyTitle.isClickable = true
            emptyTitle.clicks()
                    .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        selectedDayDate(date)
                    }
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = RecyclerViewAdapter(this, eventList)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.bringToFront()

        val itemTouchHelperCallback = ItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(recyclerView)

        recyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                        context,
                        recyclerView,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View?, position: Int) {
                                GlobalScope.async(Dispatchers.Main) {
                                    delay(RecyclerViewAdapter.CLICK_DELAY)
                                    val item = adapter.list[position] as? CopyEventDay
                                    if (item != null) {
                                        val event = RealmEventDay.select(item.key)
                                        if (event != null) {
                                            val intent = Intent(context, ActivityAddEvent::class.java)
                                            intent.putExtra("key", item.key)
                                            startActivity(intent)
//                                            Logger.d("week date: ${date.toStringDay()}, address: $this")
                                        }
                                    }
                                }
                            }

                            override fun onItemLongClick(view: View?, position: Int) {}
                        }
                )
        )

        var dialogWidth: Int = 150
        dialogHeight = 30 + 14

        dialogWidth = CalculatorUtil.dpToPx(dialogWidth.toFloat())
        dialogHeight = CalculatorUtil.dpToPx(dialogHeight.toFloat())

        if (eventList.isEmpty()) dialogHeight += MonthCalendarUiUtil.EVENT_HEIGHT
        dialogHeight += (MonthCalendarUiUtil.EVENT_HEIGHT * eventList.size)

        if (point.y + dayView.height + CalculatorUtil.dpToPx(1.0F) < weekCalendar.height) {
            if (point.y + dayView.height + dialogHeight >= weekCalendar.height - 10) {
                val height = weekCalendar.height - point.y - dayView.height - 10

                if (point.y - dayView.height > height - 10) {
                    if (point.y - dayView.height - 10 < dialogHeight) {
                        dialogHeight = point.y - dayView.height - 10
                    }
                } else {
                    dialogHeight = height
                }
            }
        } else {
            if (dialogHeight + dayView.height >= weekCalendar.height - 10) {
                dialogHeight = weekCalendar.height - dayView.height - 10
            }
        }

        eventLayout.layoutParams = ConstraintLayout.LayoutParams(
                dialogWidth,
                dialogHeight
        )

//        Logger.d("Click Point: $point")
//        Logger.d("page height: ${monthCalendar.height }")

        val set = ConstraintSet()
        set.clone(weekCalendar)

        val topMargin =
                if (point.y + dayView.height + dialogHeight >= weekCalendar.height) {
                    bottomFlag = true
                    point.y - dialogHeight
                }
                else {
                    bottomFlag = false
                    point.y + dayView.height
                }

//        Logger.d("point.x: ${point.x}")

        val startMargin =
                if (point.x + dialogWidth >= dayView.width)
                    point.x - dialogWidth
                else
                    point.x

        set.connect(eventLayout.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, topMargin)
        set.connect(eventLayout.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, startMargin)

        set.applyTo(weekCalendar)

        val animationSet = AnimationSet(false)

        val scaleAnim = ScaleAnimation(1F, 1F, 0F, 1F)
        animationSet.addAnimation(scaleAnim)

        val translateAnim = TranslateAnimation(0F, 0F, if (bottomFlag) dialogHeight.toFloat() else 0F, 0F)
        animationSet.addAnimation(translateAnim)

        animationSet.duration = ANIMATION_DURATION
        eventLayout.startAnimation(animationSet)
    }

    private fun selectedDayDate(date: Date) {
        val intent = Intent(context, ActivityAddEvent::class.java)
        intent.putExtra("startDate", date.time)
        intent.putExtra("endDate", date.time)
        startActivity(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val weekViewPager = event.map.getOrDefault(FragmentWeekViewPager.toString(), null)
        if (weekViewPager != null) {
            setAsyncPageUI(context!!)
        }

        val addEventActivity = event.map.getOrDefault(ActivityAddEvent.toStringActivity(), null)
        if (addEventActivity != null) {
            refreshPage()

            val removeDayEventView = event.map.getOrDefault("removeDayEventView", null)
            if (removeDayEventView != null) {
                removeDayEventView()
            }
        }

        val weekCalendarUiUtil = event.map.getOrDefault(WeekCalendarUiUtil.toString(), null)
        if (weekCalendarUiUtil != null) {
            if (dragInitFlag) return

            val startTime = event.map.getOrDefault("startDragDate", null) as? Long
            if (startTime != null) {

                val startDate = Date(startTime)
                val startMonth = date.startOfMonth.startOfWeek
                val endMonth = date.endOfMonth.endOfWeek

                if (startDate in startMonth..endMonth) {
                    dragStartDay = startTime
                }
            }

            val removeDayEventView = event.map.getOrDefault("removeDayEventView", null)
            if (removeDayEventView != null) {
                removeDayEventView()
            }



            val weekCalendarUiUtil = event.map.getOrDefault(WeekCalendarUiUtil.toString(), null)
            if (weekCalendarUiUtil != null) {
                if (dragInitFlag) return

                val startTime = event.map.getOrDefault("startDragDate", null) as? Long
                if (startTime != null) {

                    val startDate = Date(startTime)
                    val startMonth = date.startOfMonth.startOfWeek
                    val endMonth = date.endOfMonth.endOfWeek

                    if (startDate in startMonth..endMonth) {
                        dragStartDay = startTime
                    }
                }

                val removeDayEventView = event.map.getOrDefault("removeDayEventView", null)
                if (removeDayEventView != null) {
                    removeDayEventView()
                }

                val endTime = event.map.getOrDefault("endDragDate", null) as? Long
                val key = event.map.getOrDefault("key", null) as? Long

                if (endTime != null
                        && key != null
                        && dragStartDay > 0) {

                    var startDate = Date(dragStartDay)
                    var endDate = Date(endTime)
                    val startMonth = date.startOfMonth.startOfWeek
                    val endMonth = date.endOfMonth.endOfWeek

                    if (startDate in startMonth..endMonth
                            && endDate in startMonth..endMonth) {
//                    Logger.d("Drag start Date: ${Date(dragStartDay).toStringDay()}")
//                    Logger.d("Drag end Date: ${Date(endTime).toStringDay()}")
//                    Logger.d("key: ${Date(key).toStringDay()}")

                        val event = RealmEventDay.select(key)
                        if (event != null) {
                            dragInitFlag = true

                            var inverseFlag = false

                            if (startDate > endDate) {
                                startDate = Date(endTime)
                                endDate = Date(dragStartDay)
                                inverseFlag = true
                            }

                            var diff = 0
                            var date = startDate

                            while(date.startOfDay < endDate.startOfDay) {
                                date = date.getNextDay(1)
                                if (inverseFlag) diff-- else diff++
                            }

                            if (diff == 0) {
                                dragStartDay = 0
                                return
                            }

                            event.update(
                                    event.name,
                                    Date(event.startTime).getNextDay(diff).time,
                                    Date(event.endTime).getNextDay(diff).time,
                                    event.isComplete
                            )

                            dragStartDay = 0
                            CalendarUtil.calendarRefresh()
                        }
                    }
                }
            }


        }
    }
}