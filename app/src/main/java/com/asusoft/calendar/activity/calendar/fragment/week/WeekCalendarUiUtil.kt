package com.asusoft.calendar.activity.calendar.fragment.week

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.fragment.month.enums.WeekOfDayType
import com.asusoft.calendar.activity.calendar.fragment.week.objects.WeekItem
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.extension.removeFromSuperView
import com.asusoft.calendar.util.holiday.LunarCalendar
import com.asusoft.calendar.util.objects.CalendarUtil
import com.orhanobut.logger.Logger
import java.util.*
import kotlin.collections.ArrayList

object WeekCalendarUiUtil {

    public const val WEEK = 7
    private const val WEIGHT_SUM = 100.0F
    public const val FONT_SIZE: Float = 14.0F
    public const val COMPLETE_ALPHA = 0.4F

    fun getOneWeekUI(
            context: Context,
            startOfWeekDate: Date
    ): WeekItem {
        val rootLayout = HorizontalScrollView(context)
        rootLayout.id = View.generateViewId()
        rootLayout.isFillViewport = true

        rootLayout.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        val weekLayout = ConstraintLayout(context)
        rootLayout.addView(weekLayout)
        weekLayout.id = View.generateViewId()

        val rate: Float = 1.0F / WEEK
        var date = startOfWeekDate

        val dayViewList = ArrayList<View>()

        weekLayout.layoutParams = FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )

        for(idx in 0 until WEEK) {
            val vw = View(context)
            vw.id = View.generateViewId()
            weekLayout.addView(vw)
//            tv.setOnDragListener { v, event ->
//                MonthCalendarUIUtil.onDrag(v, event)
//            }

            vw.tag = date.time
            vw.setBackgroundColor(CalendarApplication.getColor(R.color.background))

            vw.layoutParams = ConstraintLayout.LayoutParams(
                    0,
                    0
            )
            dayViewList.add(vw)

            date = date.tomorrow
        }

        val set = ConstraintSet()
        set.clone(weekLayout)

        for (idx in dayViewList.indices) {
            val vw = dayViewList[idx]
            set.constrainPercentHeight(vw.id, rate)
            set.connect(vw.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            set.connect(vw.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

            when (idx) {
                0 -> set.connect(vw.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                else -> set.connect(vw.id, ConstraintSet.TOP, dayViewList[idx - 1].id, ConstraintSet.BOTTOM)
            }
        }

        set.applyTo(weekLayout)

        val weekItem = WeekItem(
                startOfWeekDate,
                rootLayout,
                weekLayout,
                dayViewList
        )

        addEvent(
                context,
                weekItem
        )

        return weekItem
    }

    private fun addEvent(
            context: Context,
            weekItem: WeekItem
    ) {
        weekItem.eventViewList.clear()

        for (idx in 0 until WEEK) {
            weekItem.eventViewList[idx] = HashMap()
        }

        val eventDayList = RealmEventDay.selectOneWeek(weekItem.weekDate)
        val multiDayList = eventDayList.filter { it.startTime != it.endTime }
        val oneDayList = eventDayList.filter { it.startTime == it.endTime }
        val orderMap = CalendarUtil.getEventOrderList(weekItem.weekDate, multiDayList, oneDayList, 0)

        val holidayMap = orderMap.filter { it.key <= 1231 }
        if (holidayMap.isNotEmpty()) {
            val holidayList = LunarCalendar.holidayArray("${weekItem.weekDate.calendarYear}")
            for (index in weekItem.dayViewList.indices) {
                val date = weekItem.weekDate.getNextDay(index)

                val dateString = String.format("%02d", date.calendarMonth) + String.format("%02d", date.calendarDay)
                val key = dateString.toLong()
                if (holidayMap[key] != null) {
                    val name = holidayList.first { it.date == dateString }.name
                    weekItem.addEventUI(
                            context,
                            dateString.toLong(),
                            name,
                            date.time,
                            date.time,
                            0,
                            CalendarApplication.getColor(R.color.holiday),
                            isComplete = false,
                            isHoliday = true
                    )
                }
            }
        }

        for (order in 0 until orderMap.size) {
            for (item in orderMap) {
                if (order == item.value) {
                    val eventList = eventDayList.filter { it.key == item.key }
                    if (eventList.isNotEmpty()) {
                        val event = eventList.first()
                        weekItem.addEventUI(
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

    }

    fun getOneDayView(context: Context): View {
        val inflater = LayoutInflater.from(context)
        return inflater.inflate(R.layout.view_event_of_the_week, null, false)
    }

    fun getWeekHeader(context: Context): View {
        val weekHeaderLayout = LinearLayout(context)
        weekHeaderLayout.weightSum = WEIGHT_SUM
        weekHeaderLayout.orientation = LinearLayout.VERTICAL

        weekHeaderLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )

        val days = WeekOfDayType.values()

        for (idx in 0 until WEEK) {
            val tv = TextView(context)
            weekHeaderLayout.addView(tv)

            tv.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    WEIGHT_SUM / WEEK
            )

            tv.gravity = Gravity.CENTER
            tv.tag = idx

//            if (isPopup) {
//                tv.gravity = Gravity.CENTER
//            } else {
//                val leftPadding = CalculatorUtil.dpToPx(0F)
//                tv.setPadding(leftPadding, 0, 0, 0)
//                tv.gravity = Gravity.CENTER_VERTICAL
//            }

            tv.text = days[idx].getShortTitle()
            tv.setTextColor(days[idx].getFontColor())

            tv.textSize = FONT_SIZE
        }

        return weekHeaderLayout
    }

    fun isEmptyEvent(weekItem: WeekItem): Boolean {
        for (item in weekItem.eventViewList) {
            if (item.value.size > 0) {
                return false
            }
        }

        return true
    }

    fun refreshPage(
            context: Context,
            weekItem: WeekItem
    ) {
        val removeViewList = ArrayList<View>()

        for (idx in 0 until weekItem.weekLayout.childCount) {
            val v = weekItem.weekLayout.getChildAt(idx)

            if (!weekItem.dayViewList.contains(v)) {
                removeViewList.add(v)
            }
        }

        for (v in removeViewList) {
            v.removeFromSuperView()
        }

        addEvent(
                context,
                weekItem
        )

        Logger.d("refreshPage date: ${weekItem.weekDate.toStringDay()}")
    }
}