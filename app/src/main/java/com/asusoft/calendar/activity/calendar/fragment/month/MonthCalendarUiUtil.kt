package com.asusoft.calendar.activity.calendar.fragment.month

import android.content.ClipDescription
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.Log
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.fragment.month.enums.WeekOfDayType
import com.asusoft.calendar.activity.calendar.fragment.month.objects.MonthItem
import com.asusoft.calendar.activity.calendar.fragment.month.objects.WeekOfMonthItem
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.objects.CalendarUtil.getEventOrderList
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.addBottomSeparator
import com.asusoft.calendar.util.extension.removeFromSuperView
import com.asusoft.calendar.util.holiday.LunarCalendar
import com.asusoft.calendar.util.objects.CalculatorUtil
import com.asusoft.calendar.util.objects.PreferenceKey
import com.asusoft.calendar.util.objects.PreferenceManager
import com.asusoft.calendar.util.objects.ThemeUtil
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


object MonthCalendarUiUtil {
    public const val WEEK = 7
    private const val WEIGHT_SUM = 100.0F

    private val HEADER_FONT_SIZE: Float
        get() = PreferenceManager.getFloat(PreferenceKey.MONTH_CALENDAR_HEADER_FONT_SIZE, PreferenceKey.MONTH_CALENDAR_HEADER_DEFAULT_FONT_SIZE)

    public val DAY_FONT_SIZE: Float
        get() = PreferenceManager.getFloat(PreferenceKey.MONTH_CALENDAR_DAY_FONT_SIZE, PreferenceKey.MONTH_CALENDAR_DAY_DEFAULT_FONT_SIZE)

    public val ITEM_FONT_SIZE: Float
        get() = PreferenceManager.getFloat(PreferenceKey.MONTH_CALENDAR_EVENT_FONT_SIZE, PreferenceKey.MONTH_CALENDAR_EVENT_DEFAULT_FONT_SIZE)

    private val COUNTER_FONT_SIZE: Float
        get() = PreferenceManager.getFloat(PreferenceKey.MONTH_CALENDAR_COUNTER_FONT_SIZE, PreferenceKey.MONTH_CALENDAR_COUNTER_DEFAULT_FONT_SIZE)


    public const val ALPHA = 0.4F
    public const val COMPLETE_ALPHA = 0.4F
    public const val SELECT_DAY_HEIGHT = 40.0F

    fun setTodayMarker(context: Context, weekOfMonthItem: WeekOfMonthItem, dayView: TextView): TextView {

        val todayView = TextView(context)
        todayView.id = View.generateViewId()
        todayView.setBackgroundResource(R.drawable.today_corner_radius)

        todayView.text = dayView.text
        todayView.setTextColor(ThemeUtil.instance.invertFont)
        todayView.textSize = DAY_FONT_SIZE
        todayView.setTypeface(todayView.typeface, Typeface.BOLD)
        todayView.gravity = Gravity.CENTER_HORIZONTAL

        todayView.layoutParams = ConstraintLayout.LayoutParams(
                CalculatorUtil.spToPx(DAY_FONT_SIZE + 5.0F),
                CalculatorUtil.spToPx(DAY_FONT_SIZE + 5.0F)
        )

        val weekLayout = weekOfMonthItem.weekLayout
        weekLayout.addView(todayView)

        val set = ConstraintSet()
        set.clone(weekLayout)

        val textInt = todayView.text.toString().toInt()

        val topMargin = CalculatorUtil.dpToPx(7.5F)
        val startMargin =
                if (textInt >= 10)
                    CalculatorUtil.dpToPx(6.0F)
                else
                    CalculatorUtil.dpToPx(4.0F)

        set.connect(todayView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, topMargin)
        set.connect(todayView.id, ConstraintSet.START, dayView.id, ConstraintSet.START, startMargin)

        set.applyTo(weekLayout)

        return todayView
    }

    private fun getOneWeekUI(
            context: Context,
            startOfWeekDate: Date,
            currentMonthDate: Date,
            isPopup: Boolean = false
    ): WeekOfMonthItem {
        val rootLayout = ConstraintLayout(context)
        rootLayout.id = View.generateViewId()

        val weekLayout = ConstraintLayout(context)
        rootLayout.addView(weekLayout)
        weekLayout.id = View.generateViewId()

        val rate: Float = 1.0F / WEEK
        var date = startOfWeekDate

        val dayViewList = ArrayList<TextView>()

        rootLayout.layoutParams = ConstraintLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )

        weekLayout.layoutParams = ConstraintLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )

        for(idx in 0 until WEEK) {
            val tv = TextView(context)
            tv.id = View.generateViewId()
            weekLayout.addView(tv)
            tv.setOnDragListener { v, event ->
                onDrag(v, event)
            }

            tv.text = date.calendarDay.toString()
            tv.tag = date.time
            tv.setTextColor(WeekOfDayType.fromInt(date.weekOfDay).getFontColor())
            tv.setBackgroundColor(ThemeUtil.instance.background)
            tv.textSize = DAY_FONT_SIZE
            tv.setTypeface(tv.typeface, Typeface.BOLD)

            if (isPopup) {
                tv.gravity = Gravity.CENTER
            } else {
                val padding = CalculatorUtil.dpToPx(7.5F)
                tv.setPadding(padding, padding, 0, 0)
            }

            tv.layoutParams = ConstraintLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT
            )
            dayViewList.add(tv)

            if (currentMonthDate.calendarMonth != date.calendarMonth) {
                tv.alpha = ALPHA
            }

            date = date.tomorrow
        }

        val set = ConstraintSet()
        set.clone(weekLayout)

        for (idx in dayViewList.indices) {
            val tv = dayViewList[idx]
            set.constrainPercentWidth(tv.id, rate)
            set.connect(tv.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

            when (idx) {
                0 -> set.connect(tv.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                else -> set.connect(tv.id, ConstraintSet.START, dayViewList[idx - 1].id, ConstraintSet.END)
            }
        }

        set.applyTo(weekLayout)

        return WeekOfMonthItem(startOfWeekDate, rootLayout, weekLayout, dayViewList)
    }

    fun getMonthUI(
            context: Context,
            startOfMonthDate: Date,
            isPopup: Boolean = false
    ): MonthItem {
//        val start = System.currentTimeMillis()

        val weekItemList = ArrayList<WeekOfMonthItem>()
        val row = getMonthRow(startOfMonthDate)
        var date = startOfMonthDate.startOfWeek
        val monthLayout = LinearLayout(context)
        monthLayout.id = View.generateViewId()

        monthLayout.weightSum = WEIGHT_SUM
        monthLayout.orientation = LinearLayout.VERTICAL

        if (isPopup) {
            monthLayout.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    CalculatorUtil.dpToPx((SELECT_DAY_HEIGHT * row))
            )
        } else {
            monthLayout.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

//        Logger.d("==================================")
//        Logger.d("getStatusBarHeight: ${CalculatorUtil.getStatusBarHeight()}")
//        Logger.d("getNavigationBarHeight: ${CalculatorUtil.getNavigationBarHeight()}")
//        Logger.d("getActionBarHeight: ${CalculatorUtil.getActionBarHeight()}")
//        Logger.d("getDeviceHeight: ${CalculatorUtil.getDeviceHeight()}")
//        Logger.d("getActivityHeight: ${CalculatorUtil.getActivityHeight()}")
//        Logger.d("getMonthCalendarHeight: ${CalculatorUtil.getMonthCalendarHeight()}")
//
//        Logger.d("status: ${CalculatorUtil.pxToDp(63.0F)}")
//        Logger.d("bottom: ${CalculatorUtil.pxToDp(126.0F)}")
//        Logger.d("action: ${CalculatorUtil.pxToDp(147.0F)}")
//        Logger.d("device: ${CalculatorUtil.dpToPx(683.0F)}")

        val weekHeight = (CalculatorUtil.getMonthCalendarHeight() / row) - WeekOfMonthItem.TOP_MARGIN
        val eventMaxCount = weekHeight / (CalculatorUtil.spToPx(ITEM_FONT_SIZE) + CalculatorUtil.dpToPx(7.5F))

//        Logger.d("eventMaxCount: ${eventMaxCount}")

        for (idx in 0 until row) {
            val weekOfMonthItem = getOneWeekUI(context, date, startOfMonthDate, isPopup)

            if (idx < row - 1) {
                weekOfMonthItem.rootLayout.addBottomSeparator(0.0F)
            }

            if (!isPopup) {
                addEvent(
                        context,
                        weekOfMonthItem,
                        eventMaxCount,
                )
            }

            for (index in weekOfMonthItem.dayViewList.indices) {
                val dayView = weekOfMonthItem.dayViewList[index]
                if (dayView.alpha == ALPHA) {
                    dayView.bringToFront()
                }

                if (isPopup) {
                    dayView.tag = (idx * WEEK) + index
                }
            }

            val weekLayout = weekOfMonthItem.rootLayout
            monthLayout.addView(weekLayout)

            weekLayout.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    WEIGHT_SUM / row
            )

            val padding = CalculatorUtil.dpToPx(7.5F)
            weekOfMonthItem.weekLayout.setPadding(padding, 0, padding, 0)

            weekItemList.add(weekOfMonthItem)
            date = date.nextWeek
        }

//        val diff = System.currentTimeMillis() - start
//        Logger.d("getMonthUI diff: $diff")

        return MonthItem(startOfMonthDate, monthLayout, weekItemList)
    }

    private fun addEvent(
            context: Context,
            weekOfMonthItem: WeekOfMonthItem,
            eventMaxCount: Int,
    ) {
        weekOfMonthItem.eventViewList.clear()

        for (idx in 0 until WEEK) {
            weekOfMonthItem.eventViewList[idx] = HashMap()
        }

        val eventDayList = RealmEventDay.selectOneWeek(weekOfMonthItem.weekDate)
        val multiDayList = eventDayList.filter { it.startTime != it.endTime }
        val oneDayList = eventDayList.filter { it.startTime == it.endTime }
        val orderMap = getEventOrderList(weekOfMonthItem.weekDate, multiDayList, oneDayList, eventMaxCount)

        val holidayMap = orderMap.filter { it.key <= 1231 || (it.key in 10100..123100) }
        if (holidayMap.isNotEmpty()) {
            val holidayList = LunarCalendar.holidayArray("${weekOfMonthItem.weekDate.calendarYear}")
            for (index in weekOfMonthItem.dayViewList.indices) {
                val date = weekOfMonthItem.weekDate.getNextDay(index)

                val dateString = String.format("%02d", date.calendarMonth) + String.format("%02d", date.calendarDay)
                var key = dateString.toLong()

                val toDayHolidayList = holidayList.filter { it.date == dateString }

                for (index in toDayHolidayList.indices) {
                    if (date.time < key) break
                    if (holidayMap[key] == null) break

                    weekOfMonthItem.dayViewList[index].setTextColor(ThemeUtil.instance.holiday)
                    val name = toDayHolidayList[index].name
                    weekOfMonthItem.addEventUI(
                            context,
                            dateString.toLong(),
                            name,
                            date.time,
                            date.time,
                            holidayMap[key]!!,
                            ThemeUtil.instance.holiday,
                            isComplete = false,
                            isHoliday = true
                    )

                    key *= 100
                }
            }
        }

        for (order in 0 until eventMaxCount) {
            for (item in orderMap) {
                if (order == item.value) {
                    val eventList = eventDayList.filter { it.key == item.key }
                    if (eventList.isNotEmpty()) {
                        val event = eventList.first()
                        weekOfMonthItem.addEventUI(
                            context,
                            event.key,
                            event.name,
                            event.startTime,
                            event.endTime,
                            order,
                            event.color,
                            isComplete = event.isComplete,
                            isHoliday = false
                        )
                    }
                }
            }
        }

        for (index in 0 until WEEK) {
            val item = orderMap[index.toLong()]
            if (item != null) {
                if (eventMaxCount < item) {
                    val dayView = weekOfMonthItem.dayViewList[index]
                    val countTextView = TextView(context)
                    weekOfMonthItem.weekLayout.addView(countTextView)
                    countTextView.id = View.generateViewId()

                    countTextView.text = "+${item - eventMaxCount}"
                    countTextView.setTextColor(ThemeUtil.instance.lightFont)
                    countTextView.textSize = COUNTER_FONT_SIZE
                    countTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
                    countTextView.setTypeface(countTextView.typeface, Typeface.BOLD)

                    val set = ConstraintSet()
                    set.clone(weekOfMonthItem.weekLayout)

                    val topMargin = CalculatorUtil.dpToPx(5.0F)
                    val leftMargin = CalculatorUtil.dpToPx(5.0F)

                    set.connect(countTextView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, topMargin)
                    set.connect(countTextView.id, ConstraintSet.END, dayView.id, ConstraintSet.END, leftMargin)

                    set.applyTo(weekOfMonthItem.weekLayout)
                }
            }
        }

    }

    fun refreshWeek(
            context: Context,
            weekOfMonthItem: WeekOfMonthItem,
            startOfMonthDate: Date
    ) {
        val removeViewList = ArrayList<View>()

        for (idx in 0 until weekOfMonthItem.weekLayout.childCount) {
            val v = weekOfMonthItem.weekLayout.getChildAt(idx)

            if (!weekOfMonthItem.dayViewList.contains(v)) {
                removeViewList.add(v)
            }
        }

        for (v in removeViewList) {
            v.removeFromSuperView()
        }

        val row = getMonthRow(startOfMonthDate)
        val weekHeight = (CalculatorUtil.getMonthCalendarHeight() / row) - WeekOfMonthItem.TOP_MARGIN
        val eventMaxCount = weekHeight / (CalculatorUtil.spToPx(ITEM_FONT_SIZE) + CalculatorUtil.dpToPx(7.5F))

        addEvent(
                context,
                weekOfMonthItem,
                eventMaxCount,
        )

        for (index in weekOfMonthItem.dayViewList.indices) {
            val dayView = weekOfMonthItem.dayViewList[index]
            if (dayView.alpha == ALPHA) {
                dayView.bringToFront()
            }
        }

//        Logger.d("refreshWeek")
    }

    fun setSelectedDay(
            selectedStartDate: Date? = null,
            selectedEndDate: Date? = null,
            date: Date,
            dayViewList: ArrayList<TextView>
    ) {

        val monthDate = date.startOfMonth
        val startDate = monthDate.startOfWeek
        val row = getMonthRow(monthDate)

        when {
            selectedStartDate == null
                    && selectedEndDate == null
            -> return

            selectedStartDate != null
                    && selectedEndDate == null
            -> {
                if (monthDate.startOfMonth.time != selectedStartDate.startOfMonth.time) return

                for (idx in 0 until row) {
                    val weekDate = startDate.getNextDay(idx * WEEK)

                    if (weekDate.startOfWeek.time <= selectedStartDate.time
                            && selectedStartDate.time  <= weekDate.endOfWeek.time) {
                        for (index in 0 until WEEK) {
                            val i = (idx * WEEK) + index
                            val date = startDate.getNextDay(i)
                            val dayView = dayViewList[i]

                            if (date.time == selectedStartDate.time) {
                                dayView.setBackgroundResource(R.drawable.today_corner_radius)
                            }
                        }
                    }
                }
            }

            selectedStartDate != null
                    && selectedEndDate != null
            -> {

                if (selectedStartDate.calendarYear > monthDate.calendarYear) return

                if (selectedStartDate.calendarYear == monthDate.calendarYear) {
                    if (selectedStartDate.calendarMonth > monthDate.calendarMonth) return
                }

                if (monthDate.calendarYear > selectedEndDate.calendarYear) return

                if (monthDate.calendarYear == selectedEndDate.calendarYear) {
                    if (monthDate.calendarMonth > selectedEndDate.calendarMonth) return
                }

                for (idx in 0 until row) {
                    for (index in 0 until WEEK) {
                        val i = (idx * WEEK) + index
                        val dayView = dayViewList[i]

                        if (dayView.alpha != ALPHA) {
                            val date = startDate.getNextDay(i)
                            if (selectedStartDate < date && date < selectedEndDate) {
                                dayView.setBackgroundColor(ThemeUtil.instance.today)
                            } else if (selectedStartDate == date && selectedEndDate != date) {
                                dayView.setBackgroundResource(R.drawable.corner_radius_left)
                            } else if (selectedEndDate == date && selectedStartDate != date) {
                                dayView.setBackgroundResource(R.drawable.corner_radius_right)
                            } else if (selectedEndDate == selectedStartDate && selectedStartDate == date) {
                                dayView.setBackgroundResource(R.drawable.today_corner_radius)
                            }
                        }
                    }
                }
            }
        }
    }

    fun getWeekHeader(
            context: Context,
            isPopup: Boolean = false
    ): View {
        val weekHeaderLayout = LinearLayout(context)
        weekHeaderLayout.weightSum = WEIGHT_SUM
        weekHeaderLayout.orientation = LinearLayout.HORIZONTAL

        weekHeaderLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )

        val days = WeekOfDayType.values()

        for (idx in 0 until WEEK) {
            val tv = TextView(context)
            weekHeaderLayout.addView(tv)

            tv.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    WEIGHT_SUM / WEEK
            )

            if (isPopup) {
                tv.gravity = Gravity.CENTER
            } else {
                val leftPadding = CalculatorUtil.dpToPx(7.5F)
                tv.setPadding(leftPadding, 0, 0, 0)
                tv.gravity = Gravity.CENTER_VERTICAL
            }

            tv.text = days[idx].getShortTitle()
            tv.setTextColor(days[idx].getFontColor())

            tv.textSize = HEADER_FONT_SIZE
        }

        return weekHeaderLayout
    }

    fun getMonthRow(date: Date): Int {
        val currentDate = date.startOfMonth
        var row = 5
        var day = currentDate.weekOfDay
        day += currentDate.endOfMonth.calendarDay

        if(day > WEEK * 5) {
            row = 6
        }

        return row
    }

    private fun onDrag(v: View, event: DragEvent): Boolean {
        // Defines a variable to store the action type for the incoming event
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                return if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    val vw = event.localState as View
                    vw.visibility = View.INVISIBLE
                    true
                } else {
                    false
                }
            }

            DragEvent.ACTION_DRAG_ENTERED -> {

                val backgroundColor = ThemeUtil.instance.separator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    v.background.colorFilter = BlendModeColorFilter(backgroundColor, BlendMode.SRC_IN)
                } else {
                    v.background.setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN)
                }

                if (FragmentMonthPage.dragInitFlag) {
                    FragmentMonthPage.dragInitFlag = false

                    val event = HashMapEvent(java.util.HashMap())
                    event.map[MonthCalendarUiUtil.toString()] = MonthCalendarUiUtil.toString()
                    event.map["startDragDate"] = v.tag as Long
                    GlobalBus.post(event)
                }

                v.invalidate()
                return true
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                return true
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                v.background.clearColorFilter()
                v.invalidate()

                val event = HashMapEvent(java.util.HashMap())
                event.map[MonthCalendarUiUtil.toString()] = MonthCalendarUiUtil.toString()
                event.map["removeDayEventView"] = "removeDayEventView"
                GlobalBus.post(event)

                return true
            }

            DragEvent.ACTION_DROP -> {
                v.background.clearColorFilter()
                v.invalidate()

                val vw = event.localState as? View

                if (vw != null) {
                    if (!FragmentMonthPage.dragInitFlag) {
                        val eventMap = HashMapEvent(java.util.HashMap())
                        eventMap.map[MonthCalendarUiUtil.toString()] = MonthCalendarUiUtil.toString()
                        eventMap.map["endDragDate"] = v.tag as Long
                        eventMap.map["key"] = (vw.tag as String).toLong()
                        GlobalBus.post(eventMap)
                    }
                }

                return true
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                v.background.clearColorFilter()
                v.invalidate()

                val vw = event.localState as View
                vw.visibility = View.VISIBLE

                return true
            }

            else -> Log.e("DragDrop Example", "Unknown action type received by OnDragListener.")
        }
        return false
    }
}