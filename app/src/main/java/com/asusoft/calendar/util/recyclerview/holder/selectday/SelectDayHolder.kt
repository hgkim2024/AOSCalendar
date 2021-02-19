package com.asusoft.calendar.util.recyclerview.holder.selectday

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.dialog.DialogFragmentDaySelectCalendar
import com.asusoft.calendar.fragment.month.enums.WeekOfDayType
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil.WEEK
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import java.util.HashMap

class SelectDayHolder(
        private val typeObject: Any,
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position] as SelectDayItem

        Log.d("Asu", "SelectDayHolder date: ${item.date.toStringMonth()}")

        val header = view.findViewById<TextView>(R.id.tv_header)
        header.text = item.date.toStringMonth()

        val monthDate = item.date.startOfMonth
        val startDate = monthDate.startOfWeek
        val row = MonthCalendarUIUtil.getMonthRow(monthDate)
        val dayViewList = ArrayList<TextView>()

        val bodyLayout = view.findViewById<LinearLayout>(R.id.body_layout)
        if (bodyLayout.childCount == 0) {
            val monthItem = MonthCalendarUIUtil.getMonthUI(context, item.date.startOfMonth, true)
            bodyLayout.addView(monthItem.monthView)
        } else {
            if (row == 6) {
                val lastDayCount = row * WEEK
                val dayView = bodyLayout.findViewWithTag<TextView?>(lastDayCount)
                if (dayView == null) {
                    bodyLayout.removeAllViews()
                    val monthItem = MonthCalendarUIUtil.getMonthUI(context, item.date.startOfMonth, true)
                    bodyLayout.addView(monthItem.monthView)
                }
            } else if (row == 5) {
                val lastDayCount = row * WEEK
                val dayView = bodyLayout.findViewWithTag<TextView?>(lastDayCount)
                if (dayView != null) {
                    bodyLayout.removeAllViews()
                    val monthItem = MonthCalendarUIUtil.getMonthUI(context, item.date.startOfMonth, true)
                    bodyLayout.addView(monthItem.monthView)
                }
            }
        }

        for (idx in 0 until row) {
            val weekDate = startDate.getNextDay(idx * WEEK)
            val orderMap = MonthCalendarUIUtil.getEventOrderList(weekDate)
            val holidayMap = orderMap.filter { it.key <= 1231 }

            for (index in 0 until WEEK) {
                val i = (idx * WEEK) + index
                val date = startDate.getNextDay(i)
                val dayView = bodyLayout.findViewWithTag<TextView?>(i) ?: return
                dayViewList.add(dayView)

                dayView.text = date.calendarDay.toString()
                dayView.setTextColor(WeekOfDayType.fromInt(date.weekOfDay).getFontColor(context))
                dayView.setBackgroundColor(CalendarApplication.getColor(R.color.background))

                if (date.calendarMonth == monthDate.calendarMonth) {
                    dayView.alpha = 1.0F
                } else {
                    dayView.alpha = MonthCalendarUIUtil.ALPHA
                }

                dayView.setOnClickListener {
                    if (dayView.alpha != MonthCalendarUIUtil.ALPHA) {
                        val event = HashMapEvent(HashMap())
                        event.map[SelectDayHolder.toString()] = SelectDayHolder.toString()
                        event.map["date"] = date
                        GlobalBus.getBus().post(event)
                    }
                }

                if (holidayMap.isNotEmpty()) {
                    val dateString = String.format("%02d", date.calendarMonth) + String.format("%02d", date.calendarDay)
                    val key = dateString.toLong()
                    if (holidayMap[key] != null) {
                        dayView.setTextColor(CalendarApplication.getColor(R.color.holiday))
                    }
                }
            }
        }


        val fragment = typeObject as DialogFragmentDaySelectCalendar
        MonthCalendarUIUtil.setSelectedDay(
                fragment.selectedStartDate,
                fragment.selectedEndDate,
                monthDate,
                dayViewList
        )

    }

    companion object {
        override fun toString(): String {
            return "SelectDayHolder"
        }
    }

}