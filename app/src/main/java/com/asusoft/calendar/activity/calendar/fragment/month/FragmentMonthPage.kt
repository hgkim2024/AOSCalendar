package com.asusoft.calendar.activity.calendar.fragment.month

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.addEvent.activity.ActivityAddEvent
import com.asusoft.calendar.activity.calendar.activity.ActivityCalendar
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.activity.calendar.fragment.month.objects.MonthItem
import com.asusoft.calendar.activity.calendar.fragment.month.objects.WeekItem
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.`object`.CalculatorUtil
import com.asusoft.calendar.util.`object`.CalendarUtil.getDayEventList
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil.ALPHA
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil.EVENT_HEIGHT
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil.FONT_SIZE
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil.calendarRefresh
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.getBoundsLocation
import com.asusoft.calendar.util.extension.removeFromSuperView
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.calendar.eventpopup.OneDayEventHolder
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.TimeUnit

class FragmentMonthPage: Fragment() {

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
        var dragInitFlag = true
    }

    private lateinit var date: Date
    private var monthItem: MonthItem? = null
    private lateinit var page: View
    lateinit var eventViewDate: Date

    private var prevClickDayView: View? = null
    private var prevDayEventView: ConstraintLayout? = null
    private var monthCalendar: ConstraintLayout? = null
    private var initFlag = false
    private var bottomFlag = false
    private var dialogHeight = 0
    private var preventDoubleClick = false

    private var todayView: View? = null
    private var dragStartDay = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments!!
        val time = args.getLong("time")
        initFlag = args.getBoolean("initFlag", false)

        date = Date(time)
        eventViewDate = date

        GlobalBus.register(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        GlobalBus.unregister(this)
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
        val context = context!!

        setActionBarTitle()
        setAsyncPageUI(context)

        setTodayView(context)
    }

    private fun setTodayView(context: Context) {
        if (monthItem == null) return

        val monthItem = monthItem!!

        if (todayView != null) {
            todayView?.removeFromSuperView()
            todayView = null
        }

        if (Date().getToday().startOfMonth.time == monthItem.monthDate.startOfMonth.time) {
            val today = Date().getToday()

            lateinit var weekItem: WeekItem
            lateinit var dayView: TextView

            for (item in monthItem.weekItemList) {
                val start = item.weekDate.startOfWeek
                val end = item.weekDate.endOfWeek

                if (start.time <= today.time && today.time <= end.time) {
                    weekItem = item
                    break
                }
            }

            for (index in weekItem.dayViewList.indices) {
                val date = weekItem.weekDate.getNextDay(index)

                if (date.time == today.time) {
                    dayView = weekItem.dayViewList[index]
                    if (date.startOfMonth.calendarMonth != Date().getToday().startOfMonth.calendarMonth) {
                        return
                    }
                }
            }

            todayView = MonthCalendarUIUtil.setTodayMarker(context, weekItem, dayView)
        }
    }

    private fun setAsyncPageUI(context: Context) {
        GlobalScope.async(Dispatchers.Main) {
            setPageUI(context)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setPageUI(context: Context) {
        monthCalendar = page.findViewById(R.id.month_calendar)
        if (monthCalendar?.childCount == 0) {
            monthItem = MonthCalendarUIUtil.getMonthUI(context, date.startOfMonth)
            monthCalendar?.addView(monthItem!!.monthView)
            setTodayView(context)
        }

        val monthItem: MonthItem = monthItem!!

        for (weekItem in monthItem.weekItemList) {
            for (idx in weekItem.dayViewList.indices) {
                val dayView = weekItem.dayViewList[idx]

                dayView.setOnTouchListener { v, event ->
                    when (event.action) {

                        MotionEvent.ACTION_DOWN -> {
                            if (dayView.alpha == ALPHA) {
                                dayViewClick(
                                        weekItem,
                                        dayView,
                                        idx
                                )
                            }
                        }

                        MotionEvent.ACTION_MOVE -> {}

                        MotionEvent.ACTION_UP -> {
                            if (dayView.alpha != ALPHA) {
                                dayViewClick(
                                        weekItem,
                                        dayView,
                                        idx
                                )
                            }
                        }
                    }

                    dayView.alpha != ALPHA
                }
            }
        }
    }

    private fun dayViewClick(
        weekItem: WeekItem,
        dayView: View,
        idx: Int
    ) {
        if (preventDoubleClick) return
        preventDoubleClick = true

        if (prevClickDayView != null) {
            prevClickDayView!!.setBackgroundColor(CalendarApplication.getColor(R.color.background))
        }

        if (prevDayEventView != null) {
            removeDayEventView(
                weekItem,
                dayView,
                idx
            )
        } else {
            showOneDayEventView(
                weekItem,
                dayView,
                idx
            )
        }

        if (preventDoubleClick) {
            GlobalScope.async {
                delay(ANIMATION_DURATION + 50L)
                preventDoubleClick = false
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

        val eventList = getDayEventList(date)

        title.text = "${eventList.size}개 이벤트"
        title.textSize = FONT_SIZE + 3

        addButton.clicks()
            .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                selectedDayDate(date)
            }

        if (eventList.isEmpty()) {
            emptyTitle.visibility = View.VISIBLE
            emptyTitle.textSize = FONT_SIZE + 2
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

        var dialogWidth: Int = 150
        dialogHeight = 30 + 14

        dialogWidth = CalculatorUtil.dpToPx(dialogWidth.toFloat())
        dialogHeight = CalculatorUtil.dpToPx(dialogHeight.toFloat())

        if (eventList.isEmpty()) dialogHeight += EVENT_HEIGHT
        dialogHeight += (EVENT_HEIGHT * eventList.size)

        if (point.y + dayView.height + CalculatorUtil.dpToPx(1.0F) < monthCalendar.height) {
            if (point.y + dayView.height + dialogHeight >= monthCalendar.height - 10) {
                val height = monthCalendar.height - point.y - dayView.height - 10

                if (point.y - dayView.height > height - 10) {
                    if (point.y - dayView.height - 10 < dialogHeight) {
                        dialogHeight = point.y - dayView.height - 10
                    }
                } else {
                    dialogHeight = height
                }
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

//        Logger.d("Click Point: $point")
//        Logger.d("page height: ${monthCalendar.height }")

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
            idx: Int
    ) {
        dayView.setBackgroundColor(CalendarApplication.getColor(R.color.separator))
        prevClickDayView = dayView

        val selectedDate = weekItem.weekDate.getNextDay(idx)
        eventViewDate = selectedDate

        val xPoint = dayView.getBoundsLocation()
        val yPoint = weekItem.rootLayout.getBoundsLocation()
        setOneDayEventView(
                dayView,
                weekItem.weekDate.getNextDay(idx),
                Point(xPoint.x, yPoint.y)
        )
    }

    private fun selectedDayDate(date: Date) {
        val intent = Intent(context, ActivityAddEvent::class.java)
        intent.putExtra("startDate", date.time)
        intent.putExtra("endDate", date.time)
        startActivity(intent)
    }

    private fun refreshPage() {
        if (monthItem == null) return

        val context = context!!
        for (weekItem in monthItem!!.weekItemList) {

            MonthCalendarUIUtil.refreshWeek(
                    context,
                    weekItem,
                    date
            )

            val startDate = weekItem.weekDate.startOfWeek
            val endDate = weekItem.weekDate.endOfWeek
            val today = Date().getToday()

            if (today in startDate..endDate) {
                setTodayView(context)
            }
        }
    }

    fun refreshWeek() {
        if (monthItem == null) return
        var weekItem: WeekItem? = null
        val monthItem = monthItem!!
        for (item in monthItem.weekItemList) {
            val startDate = item.weekDate.startOfWeek
            val endDate = item.weekDate.endOfWeek

            if (eventViewDate in startDate..endDate) {
                weekItem = item
                break
            }
        }

        if (weekItem == null) return

        val context = context!!
        MonthCalendarUIUtil.refreshWeek(context, weekItem, date)

        val startDate = weekItem.weekDate.startOfWeek
        val endDate = weekItem.weekDate.endOfWeek
        val today = Date().getToday()

        if (today in startDate..endDate) {
            setTodayView(context)
        }
    }

    private fun setActionBarTitle() {
        if (activity is ActivityCalendar) {
            (activity as ActivityCalendar).setTitle(date.toStringMonth())
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val addEventActivity = event.map.getOrDefault(ActivityAddEvent.toStringActivity(), null)
        if (addEventActivity != null) {
            refreshPage()

            val removeDayEventView = event.map.getOrDefault("removeDayEventView", null)
            if (removeDayEventView != null) {
                removeDayEventView()
            }
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

                val event = RealmEventDay.select(key)
                if (event != null) {
                    val intent = Intent(context, ActivityAddEvent::class.java)
                    intent.putExtra("key", key)
                    startActivity(intent)
                    return
                }
            }
        }

        val monthCalendarUIUtil = event.map.getOrDefault(MonthCalendarUIUtil.toString(), null)
        if (monthCalendarUIUtil != null) {
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
                        calendarRefresh()
                    }
                }
            }
        }

    }
}