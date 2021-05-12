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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.addEvent.activity.ActivityAddEvent
import com.asusoft.calendar.activity.calendar.activity.ActivityCalendar
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.activity.calendar.fragment.month.objects.MonthItem
import com.asusoft.calendar.activity.calendar.fragment.month.objects.WeekOfMonthItem
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.objects.CalculatorUtil
import com.asusoft.calendar.util.objects.CalendarUtil.getDayEventList
import com.asusoft.calendar.activity.calendar.fragment.month.MonthCalendarUiUtil.ALPHA
import com.asusoft.calendar.activity.calendar.fragment.month.MonthCalendarUiUtil.EVENT_HEIGHT
import com.asusoft.calendar.activity.calendar.fragment.month.MonthCalendarUiUtil.FONT_SIZE
import com.asusoft.calendar.realm.copy.CopyEventDay
import com.asusoft.calendar.util.objects.PreferenceKey
import com.asusoft.calendar.util.objects.PreferenceManager
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.getBoundsLocation
import com.asusoft.calendar.util.extension.removeFromSuperView
import com.asusoft.calendar.util.objects.CalendarUtil
import com.asusoft.calendar.util.recyclerview.RecyclerItemClickListener
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.helper.ItemTouchHelperCallback
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
    private var initFlag = false

    private var monthItem: MonthItem? = null
    private lateinit var page: View

    lateinit var eventViewDate: Date
    private var prevClickDayView: View? = null
    private var prevDayEventView: ConstraintLayout? = null
    private var preventDoubleClickFlag = true
    private var dialogHeight = 0
    private var bottomFlag = false

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
            prevClickDayView!!.setBackgroundColor(CalendarApplication.getColor(R.color.background))
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
            delay(ANIMATION_DURATION + 50L)
            preventDoubleClickFlag = true
        }
    }

    fun resizeOneDayEventView(
            eventList: ArrayList<Any>
    ) {
        val eventLayout = prevDayEventView ?: return
        val dayView = prevClickDayView ?: return

        val title = eventLayout.findViewById<TextView>(R.id.title)
        val emptyTitle = eventLayout.findViewById<TextView>(R.id.tv_empty)
        val point = dayView.getBoundsLocation()
        point.set(point.x, point.y + dayView.height)

        title.text = "${eventList.size}개 이벤트"

        if (eventList.isEmpty()) {
            emptyTitle.visibility = View.VISIBLE
        } else {
            emptyTitle.visibility = View.INVISIBLE
        }

        locationOneDayEventView(
                eventLayout,
                dayView,
                eventList,
                point
        )
    }

    private fun locationOneDayEventView(
            eventLayout: ConstraintLayout,
            dayView: View,
            eventList: ArrayList<Any>,
            point: Point
    ) {
        val monthCalendar = monthCalendar ?: return

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
        title.textSize = FONT_SIZE + 4

        addButton.clicks()
            .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                selectedDayDate(date)
            }

        if (eventList.isEmpty()) {
            emptyTitle.visibility = View.VISIBLE
            emptyTitle.textSize = FONT_SIZE + 3
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

        locationOneDayEventView(
                eventLayout,
                dayView,
                eventList,
                point
        )

        val animationSet = AnimationSet(false)

        val scaleAnim = ScaleAnimation(1F, 1F, 0F, 1F)
        animationSet.addAnimation(scaleAnim)

        val translateAnim = TranslateAnimation(0F, 0F, if (bottomFlag) dialogHeight.toFloat() else 0F, 0F)
        animationSet.addAnimation(translateAnim)

        animationSet.duration = ANIMATION_DURATION
        eventLayout.startAnimation(animationSet)
    }

    private fun removeDayEventView(
            weekOfMonthItem: WeekOfMonthItem,
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
                            weekOfMonthItem,
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
            weekOfMonthItem: WeekOfMonthItem,
            dayView: View,
            idx: Int
    ) {
        dayView.setBackgroundColor(CalendarApplication.getColor(R.color.separator))
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

    private fun selectedDayDate(date: Date) {
        val intent = Intent(context, ActivityAddEvent::class.java)
        intent.putExtra("startDate", date.time)
        intent.putExtra("endDate", date.time)
        startActivity(intent)
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