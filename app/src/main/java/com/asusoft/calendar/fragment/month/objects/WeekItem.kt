package com.asusoft.calendar.fragment.month.objects

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.asusoft.calendar.R
import com.asusoft.calendar.fragment.month.enums.WeekOfDayType
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.`object`.CalculatorUtil
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil.ALPHA
import java.util.*


class WeekItem(val weekDate: Date, val rootLayout: ConstraintLayout, val weekLayout: ConstraintLayout, val dayViewList: ArrayList<View>) {

    companion object {
        private const val EVENT_HEIGHT = 15.0F
        private const val TOP_MARGIN = 27.0F
        private const val LEFT_MARGIN = 1.0F
    }

    fun addEventUI(
            context: Context,
            name: String,
            startOfMonthDate: Date,
            startTime: Long,
            endTime: Long,
            order: Int
    ) {
        val startDay =
                if (startTime < weekDate.startOfWeek.time)
                    WeekOfDayType.fromInt(weekDate.startOfWeek.weekOfDay)
                else
                    WeekOfDayType.fromInt(Date(startTime).weekOfDay)


        val endDay =
                if (endTime < weekDate.endOfWeek.time)
                    WeekOfDayType.fromInt(Date(endTime).weekOfDay)
                else
                    WeekOfDayType.fromInt(weekDate.endOfWeek.weekOfDay)

        val isOneDay = startDay == endDay

        if (endDay.value < startDay.value) return

        // UI 생성
        val eventView: View = when {

            // 하루 이벤트 UI
            isOneDay -> {
                val eventView = MonthCalendarUIUtil.getEventView(context, name, false)
                val curDate = Date(startTime)
                if (startOfMonthDate.calendarMonth != curDate.calendarMonth
                    || startOfMonthDate.calendarYear != curDate.calendarYear) {
                    eventView.alpha = ALPHA
                }

                eventView
            }

            // 이틀 이상 이벤트 UI
            else -> {
                // TODO: - 이틀 이상 뷰도 현재 달이 아닌 경우 알파처리
                val eventView = TextView(context)

                eventView.textSize = MonthCalendarUIUtil.FONT_SIZE
                eventView.gravity = Gravity.CENTER_VERTICAL
                eventView.maxLines = 1
                eventView.text = name
                eventView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
                eventView.setTextColor(ContextCompat.getColor(context, R.color.invertFont))
                eventView
            }
        }

        // UI 배치
        eventView.id = View.generateViewId()

        eventView.layoutParams = ConstraintLayout.LayoutParams(
                0,
                CalculatorUtil.dpToPx(EVENT_HEIGHT)
        )
        weekLayout.addView(eventView)

        val startDayView = dayViewList[startDay.value]
        val endDayView = dayViewList[endDay.value]

        val set = ConstraintSet()
        set.clone(weekLayout)

        val padding = CalculatorUtil.dpToPx(LEFT_MARGIN)
        eventView.setPadding(padding, 0, padding, 0)

        val topMargin = CalculatorUtil.dpToPx(TOP_MARGIN + (order * (EVENT_HEIGHT + 2)))
        val leftMargin = CalculatorUtil.dpToPx(LEFT_MARGIN)

        set.connect(eventView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, topMargin)
        set.connect(eventView.id, ConstraintSet.START, startDayView.id, ConstraintSet.START, leftMargin)
        set.connect(eventView.id, ConstraintSet.END, endDayView.id, ConstraintSet.END, leftMargin)

        set.applyTo(weekLayout)
    }
}
