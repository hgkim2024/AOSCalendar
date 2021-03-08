package com.asusoft.calendar.util.`object`

import android.R.color
import android.content.ClipDescription
import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.fragment.month.FragmentMonthPage
import com.asusoft.calendar.fragment.month.enums.WeekOfDayType
import com.asusoft.calendar.fragment.month.objects.MonthItem
import com.asusoft.calendar.fragment.month.objects.WeekItem
import com.asusoft.calendar.realm.RealmEventMultiDay
import com.asusoft.calendar.realm.RealmEventOneDay
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.addBottomSeparator
import com.asusoft.calendar.util.extension.removeFromSuperView
import com.asusoft.calendar.util.holiday.LunarCalendar
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.delete.DeleteHolder
import com.orhanobut.logger.Logger
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


object MonthCalendarUIUtil {
    public const val WEEK = 7
    private const val WEIGHT_SUM = 100.0F

    public const val FONT_SIZE = 12F
    public const val EVENT_HEIGHT = 26.0F
    public const val ALPHA = 0.5F
    public const val COMPLETE_ALPHA = 0.5F
    public const val SELECT_DAY_HEIGHT = 40.0F

    fun setTodayMarker(context: Context, weekItem: WeekItem, dayView: TextView): TextView {

        val todayView = TextView(context)
        todayView.id = View.generateViewId()
        todayView.setBackgroundResource(R.drawable.today_corner_radius)

        todayView.text = dayView.text
        todayView.setTextColor(CalendarApplication.getColor(R.color.invertFont))
        todayView.textSize = FONT_SIZE
        todayView.setTypeface(todayView.typeface, Typeface.BOLD)
        todayView.gravity = Gravity.CENTER_HORIZONTAL

        todayView.layoutParams = ConstraintLayout.LayoutParams(
                CalculatorUtil.dpToPx(FONT_SIZE + 4.0F),
                CalculatorUtil.dpToPx(FONT_SIZE + 4.0F)
        )

        val weekLayout = weekItem.weekLayout
        weekLayout.addView(todayView)

        val set = ConstraintSet()
        set.clone(weekLayout)

        val textInt = todayView.text.toString().toInt()

        val topMargin = CalculatorUtil.dpToPx(8.0F)
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

    fun getDayEventList(date: Date, isHoliday: Boolean = true): ArrayList<Any> {
        val eventList: ArrayList<Any> = ArrayList()
        val oneDayCopyList = RealmEventOneDay.getOneDayCopyList(date)
        val multiDayCopyList = RealmEventMultiDay.getOneDayCopyList(date)
        val orderMap = getEventOrderList(date)

        var order = 0

        if (isHoliday) {
            val dateString = String.format("%02d", date.calendarMonth) + String.format("%02d", date.calendarDay)
            val holidayMap = orderMap.filter { it.key <= 1231 }

            if (holidayMap.isNotEmpty()) {
                val holidayList = LunarCalendar.holidayArray("${date.calendarYear}")
                if (holidayMap[dateString.toLong()] != null) {
                    val name = holidayList.first { it.date == dateString }.name
                    eventList.add(name)
                }
            }
        }

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

    fun getEventOrderList(
            weekDate: Date
    ): HashMap<Long, Int> {
        val multiDayList = RealmEventMultiDay.selectOneWeek(weekDate.startOfWeek)
        val oneDayList = RealmEventOneDay.selectOneWeek(weekDate.endOfWeek)

        return getEventOrderList(
                weekDate,
                multiDayList,
                oneDayList
        )
    }


    private fun getEventOrderList(
            weekDate: Date,
            realmEventMultiDayList: List<RealmEventMultiDay>,
            realmEventOneDayList: List<RealmEventOneDay>,
            eventMaxCount: Int = 5
    ): HashMap<Long, Int> {

        val startDateString = String.format("%02d", weekDate.startOfWeek.calendarMonth) + String.format("%02d", weekDate.startOfWeek.calendarDay)
        val endDateString = String.format("%02d", weekDate.endOfWeek.calendarMonth) + String.format("%02d", weekDate.endOfWeek.calendarDay)

        val holidayList = if (weekDate.startOfWeek.calendarMonth == 12 && weekDate.endOfWeek.calendarMonth == 1) {
            LunarCalendar.holidayArray("${weekDate.calendarYear}").filter { it.date <= endDateString }
        } else {
            LunarCalendar.holidayArray("${weekDate.calendarYear}").filter { it.date in startDateString..endDateString }
        }

        val orderMap = HashMap<Long, Int>()
        val dayCheckList = ArrayList<Array<Boolean>>()
        dayCheckList.add(arrayOf(false, false, false, false, false, false, false))

        if (holidayList.isNotEmpty()) {
            for (item in holidayList) {
                val sdf = SimpleDateFormat("yyyyMMdd")
                val cal = Calendar.getInstance()
                cal.time = sdf.parse(item.year + item.date)
                val weekOfDay = cal.time.weekOfDay

//                Logger.d("holiday: ${item.date}, ${item.name}, weekDay: ${weekDate.toStringDay()}")

                orderMap[item.date.toLong()] = 0
                dayCheckList[0][weekOfDay] = true
            }
        }

        orderMultiDay(
                weekDate,
                realmEventMultiDayList.filter { !it.isComplete },
                dayCheckList,
                orderMap
        )

        orderOneDay(
                realmEventOneDayList.filter { !it.isComplete },
                dayCheckList,
                orderMap
        )

        orderMultiDay(
                weekDate,
                realmEventMultiDayList.filter { it.isComplete },
                dayCheckList,
                orderMap
        )

        orderOneDay(
                realmEventOneDayList.filter { it.isComplete },
                dayCheckList,
                orderMap
        )

        if (dayCheckList.size > eventMaxCount) {
            for (idx in dayCheckList.indices) {
                for (index in dayCheckList[idx].indices) {
                    if (dayCheckList[idx][index]) {
                        orderMap[index.toLong()] = idx + 1
                    }
                }
            }
        }

        return orderMap
    }

    private fun orderMultiDay(
            weekDate: Date,
            realmEventMultiDayList: List<RealmEventMultiDay>,
            dayCheckList: ArrayList<Array<Boolean>>,
            orderMap: HashMap<Long, Int>
    ) {
        for (eventMultiDay in realmEventMultiDayList) {
            val startOfWeek = if (eventMultiDay.startTime < weekDate.startOfWeek.time) {
                weekDate.startOfWeek.weekOfDay
            } else {
                Date(eventMultiDay.startTime).weekOfDay
            }

            val endOfWeek = if (eventMultiDay.endTime < weekDate.endOfWeek.time) {
                Date(eventMultiDay.endTime).weekOfDay
            } else {
                weekDate.endOfWeek.weekOfDay
            }

            var index = 0
            loop@ while(true) {
                if (startOfWeek >= endOfWeek) break@loop

                if (dayCheckList.size <= index) {
                    dayCheckList.add(arrayOf(false, false, false, false, false, false, false))
                }

                var breakFlag = true

                for (i in startOfWeek..endOfWeek) {
                    if (dayCheckList[index][i]) {
                        breakFlag = false
                        break
                    }
                }

                if (breakFlag) {
                    orderMap[eventMultiDay.key] = index
                    for (i in startOfWeek..endOfWeek) {
                        dayCheckList[index][i] = true
                    }

                    break@loop
                }

                index++
            }
        }
    }

    private fun orderOneDay(
            realmEventOneDayList: List<RealmEventOneDay>,
            dayCheckList: ArrayList<Array<Boolean>>,
            orderMap: HashMap<Long, Int>
    ) {
        for (eventOneDay in realmEventOneDayList) {
            val weekOfDay = Date(eventOneDay.time).weekOfDay

            var index = 0
            loop@ while(true) {
                if (dayCheckList.size <= index) {
                    dayCheckList.add(arrayOf(false, false, false, false, false, false, false))
                }

                if (!dayCheckList[index][weekOfDay]) {
                    orderMap[eventOneDay.key] = index
                    dayCheckList[index][weekOfDay] = true

                    break@loop
                }

                index++
            }
        }
    }

    fun getEventView(
            context: Context
    ): ConstraintLayout {
        val eventLayout = ConstraintLayout(context)
        val edgeView = View(context)
        val textView = TextView(context)
        val checkBox = CheckBox(context)

        eventLayout.id = View.generateViewId()
        edgeView.id = View.generateViewId()
        textView.id = View.generateViewId()
        checkBox.id = View.generateViewId()

        edgeView.tag = 0
        textView.tag = 1
        checkBox.tag = 2

        eventLayout.addView(edgeView)
        eventLayout.addView(textView)
        eventLayout.addView(checkBox)

        eventLayout.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                CalculatorUtil.dpToPx(EVENT_HEIGHT)
        )

        edgeView.layoutParams = ConstraintLayout.LayoutParams(
                CalculatorUtil.dpToPx(4.0F),
                0
        )

        val startPadding = CalculatorUtil.dpToPx(3.0F)
        textView.setPadding(startPadding, 0, startPadding, 0)
        textView.textSize = MonthCalendarUIUtil.FONT_SIZE + 2
        textView.setSingleLine()
        textView.ellipsize = TextUtils.TruncateAt.END

        textView.layoutParams = ConstraintLayout.LayoutParams(
                0,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        textView.setTextColor(CalendarApplication.getColor(R.color.font))
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.maxLines = 1

        checkBox.buttonTintList = CalendarApplication.getColorList(R.color.colorAccent)
        checkBox.alpha = 0.7F

        checkBox.layoutParams = ConstraintLayout.LayoutParams(
                CalculatorUtil.dpToPx(35.0F),
                ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        val set = ConstraintSet()
        set.clone(eventLayout)

        val topMargin = CalculatorUtil.dpToPx(2.0F)
        val startMargin = CalculatorUtil.dpToPx(7.0F)

        set.connect(edgeView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, topMargin)
        set.connect(edgeView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, topMargin)
        set.connect(edgeView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, startMargin)

        set.connect(textView.id, ConstraintSet.START, edgeView.id, ConstraintSet.END)
        set.connect(textView.id, ConstraintSet.END, checkBox.id, ConstraintSet.START)

        set.connect(checkBox.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        set.applyTo(eventLayout)

        return eventLayout
    }

    private fun getOneWeekUI(
            context: Context,
            startOfWeekDate: Date,
            currentMonthDate: Date,
            isPopup: Boolean = false
    ): WeekItem {
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
            tv.setTextColor(WeekOfDayType.fromInt(date.weekOfDay).getFontColor(context))
            tv.setBackgroundColor(CalendarApplication.getColor(R.color.background))
            tv.textSize = FONT_SIZE
            tv.setTypeface(tv.typeface, Typeface.BOLD)

            if (isPopup) {
                tv.gravity = Gravity.CENTER
            } else {
                val padding = CalculatorUtil.dpToPx(8.0F)
                tv.setPadding(padding, padding, 0, 0)
            }

            tv.layoutParams = ConstraintLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT
            )

            val set = ConstraintSet()
            set.clone(weekLayout)

            set.constrainPercentWidth(tv.id, rate)
            set.connect(tv.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

            when(idx) {
                0 -> set.connect(tv.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                else -> set.connect(tv.id, ConstraintSet.START, dayViewList.last().id, ConstraintSet.END)
            }

            set.applyTo(weekLayout)
            dayViewList.add(tv)

            if (currentMonthDate.calendarMonth != date.calendarMonth) {
                tv.alpha = ALPHA
            }

            date = date.tomorrow
        }

        return WeekItem(startOfWeekDate, rootLayout, weekLayout, dayViewList)
    }

    fun getMonthUI(
            context: Context,
            startOfMonthDate: Date,
            isPopup: Boolean = false
    ): MonthItem {
//        val start = System.currentTimeMillis()

        val weekItemList = ArrayList<WeekItem>()
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

        val weekHeight = (CalculatorUtil.getMonthCalendarHeight() / row) - CalculatorUtil.dpToPx(WeekItem.TOP_MARGIN)
        val eventMaxCount = weekHeight / CalculatorUtil.dpToPx(WeekItem.EVENT_HEIGHT)

//        Logger.d("eventMaxCount: ${eventMaxCount}")

        for (idx in 0 until row) {
            val weekItem = getOneWeekUI(context, date, startOfMonthDate, isPopup)

            if (idx < row - 1) {
                weekItem.rootLayout.addBottomSeparator(0.0F)
            }

            if (!isPopup) {
                addEvent(
                        context,
                        weekItem,
                        eventMaxCount,
                )
            }

            for (index in weekItem.dayViewList.indices) {
                val dayView = weekItem.dayViewList[index]
                if (dayView.alpha == ALPHA) {
                    dayView.bringToFront()
                }

                if (isPopup) {
                    dayView.tag = (idx * WEEK) + index
                }
            }

            val weekLayout = weekItem.rootLayout
            monthLayout.addView(weekLayout)

            weekLayout.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    WEIGHT_SUM / row
            )

            val padding = CalculatorUtil.dpToPx(8.0F)
            weekItem.weekLayout.setPadding(padding, 0, padding, 0)

            weekItemList.add(weekItem)
            date = date.nextWeek
        }

//        val diff = System.currentTimeMillis() - start
//        Logger.d("getMonthUI diff: $diff")

        return MonthItem(startOfMonthDate, monthLayout, weekItemList)
    }

    private fun addEvent(
            context: Context,
            weekItem: WeekItem,
            eventMaxCount: Int,
    ) {
        val multiDayList = RealmEventMultiDay.selectOneWeek(weekItem.weekDate)
        val oneDayList = RealmEventOneDay.selectOneWeek(weekItem.weekDate)
        val orderMap = getEventOrderList(weekItem.weekDate, multiDayList, oneDayList, eventMaxCount)

        val holidayMap = orderMap.filter { it.key <= 1231 }
        if (holidayMap.isNotEmpty()) {
            val holidayList = LunarCalendar.holidayArray("${weekItem.weekDate.calendarYear}")
            for (index in weekItem.dayViewList.indices) {
                val date = weekItem.weekDate.getNextDay(index)

                val dateString = String.format("%02d", date.calendarMonth) + String.format("%02d", date.calendarDay)
                val key = dateString.toLong()
                if (holidayMap[key] != null) {
                    weekItem.dayViewList[index].setTextColor(CalendarApplication.getColor(R.color.holiday))

                    val name = holidayList.first { it.date == dateString }.name
                    weekItem.addEventUI(
                            context,
                            dateString.toLong(),
                            name,
                            date.time,
                            date.time,
                            0,
                            isComplete = false,
                            isHoliday = true
                    )
                }
            }
        }

        for (multiDay in multiDayList) {
            val order = orderMap.getOrDefault(multiDay.key, -1)
            if (eventMaxCount <= order) continue

            if (order != -1) {
                weekItem.addEventUI(
                        context,
                        multiDay.key,
                        multiDay.name,
                        multiDay.startTime,
                        multiDay.endTime,
                        order,
                        isComplete = multiDay.isComplete,
                        isHoliday = false
                )
            }
        }

        for (oneDay in oneDayList) {
            val order = orderMap.getOrDefault(oneDay.key, -1)
            if (eventMaxCount <= order) continue

            if (order != -1) {
                weekItem.addEventUI(
                        context,
                        oneDay.key,
                        oneDay.name,
                        oneDay.time,
                        oneDay.time,
                        order,
                        isComplete = oneDay.isComplete,
                        isHoliday = false
                )
            }
        }

        for (index in 0 until WEEK) {
            val item = orderMap[index.toLong()]
            if (item != null) {
                if (eventMaxCount < item) {
                    val dayView = weekItem.dayViewList[index]
                    val countTextView = TextView(context)
                    weekItem.weekLayout.addView(countTextView)
                    countTextView.id = View.generateViewId()

                    countTextView.text = "+${item - eventMaxCount}"
                    countTextView.setTextColor(CalendarApplication.getColor(R.color.lightFont))
                    countTextView.textSize = 11.0F
                    countTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
                    countTextView.setTypeface(countTextView.typeface, Typeface.BOLD)

                    val set = ConstraintSet()
                    set.clone(weekItem.weekLayout)

                    val topMargin = CalculatorUtil.dpToPx(5.0F)
                    val leftMargin = CalculatorUtil.dpToPx(5.0F)

                    set.connect(countTextView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, topMargin)
                    set.connect(countTextView.id, ConstraintSet.END, dayView.id, ConstraintSet.END, leftMargin)

                    set.applyTo(weekItem.weekLayout)
                }
            }
        }

    }

    fun refreshWeek(
            context: Context,
            weekItem: WeekItem,
            startOfMonthDate: Date
    ) {
        val removeViewList = ArrayList<View>()

        for (idx in 0 until weekItem.weekLayout.childCount) {
            val v = weekItem.weekLayout.getChildAt(idx)

            var addFlag = true
            for (dayView in weekItem.dayViewList) {
                if (v == dayView) {
                    addFlag = false
                    break
                }
            }

            if (addFlag) {
                removeViewList.add(v)
            }
        }

        for (v in removeViewList) {
            v.removeFromSuperView()
        }

        val row = getMonthRow(startOfMonthDate)
        val weekHeight = (CalculatorUtil.getMonthCalendarHeight() / row) - CalculatorUtil.dpToPx(WeekItem.TOP_MARGIN)
        val eventMaxCount = weekHeight / CalculatorUtil.dpToPx(WeekItem.EVENT_HEIGHT)

        addEvent(
                context,
                weekItem,
                eventMaxCount,
        )

        for (index in weekItem.dayViewList.indices) {
            val dayView = weekItem.dayViewList[index]
            if (dayView.alpha == ALPHA) {
                dayView.bringToFront()
            }
        }
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
                                dayView.setBackgroundColor(CalendarApplication.getColor(R.color.selectDay))
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
                val leftPadding = CalculatorUtil.dpToPx(8.0F)
                tv.setPadding(leftPadding, 0, 0, 0)
                tv.gravity = Gravity.CENTER_VERTICAL
            }

            tv.text = days[idx].getShortTitle()
            tv.setTextColor(days[idx].getFontColor(context))

            tv.textSize = FONT_SIZE
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

    fun setCornerRadiusDrawable(v: View, backgroundColor: Int) {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        val r = CalculatorUtil.dpToPx(2.0F).toFloat()
        shape.cornerRadii = floatArrayOf(r, r, r, r, r, r, r, r)
        shape.setColor(backgroundColor)
//        shape.setStroke(3, borderColor)
        v.background = shape
    }

    private fun onDrag(v: View, event: DragEvent): Boolean {
        // Defines a variable to store the action type for the incoming event
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                // Determines if this View can accept the dragged data
                return if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    // if you want to apply color when drag started to your view you can uncomment below lines
                    // to give any color tint to the View to indicate that it can accept data.
                    // v.getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
                    // Invalidate the view to force a redraw in the new tint
                    //  v.invalidate();
                    // returns true to indicate that the View can accept the dragged data.
                    true
                } else false
                // Returns false. During the current drag and drop operation, this View will
                // not receive events again until ACTION_DRAG_ENDED is sent.
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                // Applies a GRAY or any color tint to the View. Return true; the return value is ignored.

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    v.background.colorFilter = BlendModeColorFilter(Color.GRAY, BlendMode.SRC_IN)
                } else {
                    v.background.setColorFilter(CalendarApplication.getColor(R.color.separator), PorterDuff.Mode.SRC_IN)
                }

                if (FragmentMonthPage.dragInitFlag) {
                    FragmentMonthPage.dragInitFlag = false

                    val event = HashMapEvent(java.util.HashMap())
                    event.map[MonthCalendarUIUtil.toString()] = MonthCalendarUIUtil.toString()
                    event.map["startDragDate"] = v.tag as Long
                    GlobalBus.getBus().post(event)
                }

                // Invalidate the view to force a redraw in the new tint
                v.invalidate()
                return true
            }
            DragEvent.ACTION_DRAG_LOCATION ->                 // Ignore the event
                return true
            DragEvent.ACTION_DRAG_EXITED -> {
                // Re-sets the color tint to blue. Returns true; the return value is ignored.
                // view.getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
                //It will clear a color filter .
                v.background.clearColorFilter()
                // Invalidate the view to force a redraw in the new tint
                v.invalidate()
                return true
            }
            DragEvent.ACTION_DROP -> {
                // Gets the item containing the dragged data
                val item = event.clipData.getItemAt(0)
                // Gets the text data from the item.
                val dragData = item.text.toString()
                // Displays a message containing the dragged data.
//                Toast.makeText(this, "Dragged data is $dragData", Toast.LENGTH_SHORT).show()
                // Turns off any color tints
                v.background.clearColorFilter()
                // Invalidates the view to force a redraw
                v.invalidate()

                val vw = event.localState as View

//                val owner = vw.parent as ViewGroup
//                owner.removeView(vw) //remove the dragged view
                //caste the view into LinearLayout as our drag acceptable layout is LinearLayout
//                val container = v.parent as ConstraintLayout
//                container.addView(vw) //Add the dragged view
//                vw.visibility = View.VISIBLE //finally set Visibility to VISIBLE
                // Returns true. DragEvent.getResult() will return true.

                if (!FragmentMonthPage.dragInitFlag) {
                    val event = HashMapEvent(java.util.HashMap())
                    event.map[MonthCalendarUIUtil.toString()] = MonthCalendarUIUtil.toString()
                    event.map["endDragDate"] = v.tag as Long
                    event.map["key"] = (vw.tag as String).toLong()
                    GlobalBus.getBus().post(event)
                }

                return true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                // Turns off any color tinting
                v.background.clearColorFilter()
                // Invalidates the view to force a redraw
                v.invalidate()
                // Does a getResult(), and displays what happened.
//                if (event.result) Toast.makeText(this, "The drop was handled.", Toast.LENGTH_SHORT).show() else Toast.makeText(this, "The drop didn't work.", Toast.LENGTH_SHORT).show()
                // returns true; the value is ignored.
                return true
            }
            else -> Log.e("DragDrop Example", "Unknown action type received by OnDragListener.")
        }
        return false
    }
}