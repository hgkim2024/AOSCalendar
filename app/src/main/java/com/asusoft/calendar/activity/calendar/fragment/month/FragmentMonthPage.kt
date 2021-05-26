package com.asusoft.calendar.activity.calendar.fragment.month

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.addEvent.activity.ActivityAddEvent
import com.asusoft.calendar.activity.calendar.activity.ActivityCalendar
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.activity.calendar.fragment.month.objects.MonthItem
import com.asusoft.calendar.activity.calendar.fragment.month.objects.WeekOfMonthItem
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.util.*
import com.asusoft.calendar.activity.calendar.fragment.month.MonthCalendarUiUtil.ALPHA
import com.asusoft.calendar.util.objects.PreferenceKey
import com.asusoft.calendar.util.objects.PreferenceManager
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.getBoundsLocation
import com.asusoft.calendar.util.extension.removeFromSuperView
import com.asusoft.calendar.util.objects.CalendarUtil
import com.asusoft.calendar.util.objects.ThemeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

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

        var dragInitFlag = true
    }

    private lateinit var date: Date
    private var initFlag = false

    private var monthItem: MonthItem? = null
    private lateinit var page: View

    lateinit var eventViewDate: Date
    private var prevClickDayView: View? = null
    private var prevDayEventView: ConstraintLayout? = null
    private var preventDoubleClickFlag = true

    var dialogHeight = 0
    var bottomFlag = false

    private var monthCalendar: ConstraintLayout? = null

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

            lateinit var weekOfMonthItem: WeekOfMonthItem
            lateinit var dayView: TextView

            for (item in monthItem.weekOfMonthItemList) {
                val start = item.weekDate.startOfWeek
                val end = item.weekDate.endOfWeek

                if (start.time <= today.time && today.time <= end.time) {
                    weekOfMonthItem = item
                    break
                }
            }

            for (index in weekOfMonthItem.dayViewList.indices) {
                val date = weekOfMonthItem.weekDate.getNextDay(index)

                if (date.time == today.time) {
                    dayView = weekOfMonthItem.dayViewList[index]
                    if (date.startOfMonth.calendarMonth != Date().getToday().startOfMonth.calendarMonth) {
                        return
                    }
                }
            }

            todayView = MonthCalendarUiUtil.setTodayMarker(context, weekOfMonthItem, dayView)
        }
    }

    private fun setAsyncPageUI(context: Context) {
        GlobalScope.async(Dispatchers.Main) {
            setPageUI(context)
        }
    }

    private fun setPageUI(context: Context) {
        monthCalendar = page.findViewById(R.id.calendar)
        if (monthItem == null) {
            monthItem = MonthCalendarUiUtil.getMonthUI(context, date.startOfMonth)
            monthCalendar?.addView(monthItem!!.monthView)
            setTodayView(context)
        }

        val monthItem: MonthItem = monthItem!!

        for (weekItem in monthItem.weekOfMonthItemList) {
            for (idx in weekItem.dayViewList.indices) {
                val dayView = weekItem.dayViewList[idx]
                setDayViewTouchEvent(
                        dayView,
                        weekItem,
                        idx
                )
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setDayViewTouchEvent(
            dayView: View,
            weekOfMonthItem: WeekOfMonthItem,
            idx: Int
    ) {
        val dragFlag = PreferenceManager.getBoolean(PreferenceKey.MONTH_CALENDAR_DRAG_AND_DROP, PreferenceKey.DRAG_AND_DROP_DEFAULT)

        if (dragFlag) {
            dayView.setOnTouchListener { v, event ->
                when (event.action) {

                    MotionEvent.ACTION_DOWN -> {
                        if (dayView.alpha == ALPHA) {
                            dayViewClick(
                                    weekOfMonthItem,
                                    dayView,
                                    idx
                            )
                        }
                    }

                    MotionEvent.ACTION_MOVE -> { }

                    MotionEvent.ACTION_UP -> {
                        if (dayView.alpha != ALPHA) {
                            dayViewClick(
                                    weekOfMonthItem,
                                    dayView,
                                    idx
                            )
                        }
                    }
                }

                dayView.alpha != ALPHA
            }
        } else {
            dayView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> { }
                    MotionEvent.ACTION_MOVE -> { }
                    MotionEvent.ACTION_UP -> {
                        dayViewClick(
                                weekOfMonthItem,
                                dayView,
                                idx
                        )
                    }
                }
                true
            }
        }
    }

    private fun dayViewClick(
            weekOfMonthItem: WeekOfMonthItem,
            dayView: View,
            idx: Int
    ) {
        if (!preventDoubleClickFlag) return
        preventDoubleClickFlag = false

        if (prevClickDayView != null) {
            prevClickDayView!!.setBackgroundColor(ThemeUtil.instance.background)
        }

        if (prevDayEventView != null) {
            removeDayEventView(
                weekOfMonthItem,
                dayView,
                idx
            )
        } else {
            showOneDayEventView(
                weekOfMonthItem,
                dayView,
                idx
            )
        }

        GlobalScope.async {
            delay(CalendarUtil.ANIMATION_DURATION + 100L)
            preventDoubleClickFlag = true
        }
    }

    fun resizeOneDayEventView(
            eventList: ArrayList<Any>
    ) {
        val calendar = monthCalendar ?: return
        val eventLayout = prevDayEventView ?: return
        val dayView = prevClickDayView ?: return

        val title = eventLayout.findViewById<TextView>(R.id.title)
        val emptyTitle = eventLayout.findViewById<TextView>(R.id.tv_empty)
        val xPoint = dayView.getBoundsLocation()
        val rootLayout = ((dayView.parent as? ViewGroup)?.parent as? ViewGroup) ?: return
        val yPoint = rootLayout.getBoundsLocation()

        title.text = "${eventList.size}개 이벤트"

        if (eventList.isEmpty()) {
            emptyTitle.visibility = View.VISIBLE
        } else {
            emptyTitle.visibility = View.INVISIBLE
        }

        CalendarUtil.locationOneDayEventLayout(
                this,
                calendar,
                eventLayout,
                dayView,
                eventList,
                Point(xPoint.x, yPoint.y)
        )
    }

    private fun setOneDayEventView(
            dayView: View,
            date: Date,
            point: Point
    ) {
        val context = context ?: return
        val calendar = monthCalendar ?: return

        val eventList = CalendarUtil.getDayEventList(date)

        val eventLayout = CalendarUtil.getOneDayEventLayout(this, context, calendar, eventList, date)
        prevDayEventView = eventLayout

        CalendarUtil.locationOneDayEventLayout(
                this,
                calendar,
                eventLayout,
                dayView,
                eventList,
                point
        )

        CalendarUtil.showOneDayEventLayoutAnimation(
                eventLayout,
                bottomFlag,
                dialogHeight
        )
    }

    private fun removeDayEventView(
            weekOfMonthItem: WeekOfMonthItem,
            dayView: View,
            idx: Int
    ) {
        if (prevDayEventView != null) {
            var view = prevDayEventView
            prevDayEventView = null

            val animationSet = CalendarUtil.getHideOneDayEventLayoutAnimationSet(
                    bottomFlag,
                    dialogHeight
            )

            animationSet.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    view?.removeFromSuperView()
                    view = null

                    if (prevClickDayView == dayView) return

                    showOneDayEventView(
                            weekOfMonthItem,
                            dayView,
                            idx
                    )
                }
            })

            view?.startAnimation(animationSet)
        }
    }

    private fun removeDayEventView() {
        if (prevClickDayView != null) {
            prevClickDayView!!.setBackgroundColor(ThemeUtil.instance.background)
            prevClickDayView = null
        }

        if (prevDayEventView != null) {
            prevDayEventView!!.removeFromSuperView()
            prevDayEventView = null
        }
    }

    private fun showOneDayEventView(
            weekOfMonthItem: WeekOfMonthItem,
            dayView: View,
            idx: Int
    ) {
        dayView.setBackgroundColor(ThemeUtil.instance.separator)
        prevClickDayView = dayView

        val selectedDate = weekOfMonthItem.weekDate.getNextDay(idx)
        eventViewDate = selectedDate

        val xPoint = dayView.getBoundsLocation()
        val yPoint = weekOfMonthItem.rootLayout.getBoundsLocation()
        setOneDayEventView(
                dayView,
                weekOfMonthItem.weekDate.getNextDay(idx),
                Point(xPoint.x, yPoint.y)
        )
    }

    private fun refreshPage() {
        if (monthItem == null) return

        val context = context!!
        for (weekItem in monthItem!!.weekOfMonthItemList) {

            MonthCalendarUiUtil.refreshWeek(
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
        var weekOfMonthItem: WeekOfMonthItem? = null
        val monthItem = monthItem!!
        for (item in monthItem.weekOfMonthItemList) {
            val startDate = item.weekDate.startOfWeek
            val endDate = item.weekDate.endOfWeek

            if (eventViewDate in startDate..endDate) {
                weekOfMonthItem = item
                break
            }
        }

        if (weekOfMonthItem == null) return

        val context = context!!
        MonthCalendarUiUtil.refreshWeek(context, weekOfMonthItem, date)

        val startDate = weekOfMonthItem.weekDate.startOfWeek
        val endDate = weekOfMonthItem.weekDate.endOfWeek
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

        val monthCalendarUIUtil = event.map.getOrDefault(MonthCalendarUiUtil.toString(), null)
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
                        CalendarUtil.calendarRefresh()
                    }
                }
            }
        }

    }
}