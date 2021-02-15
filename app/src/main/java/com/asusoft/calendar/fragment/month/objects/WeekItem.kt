package com.asusoft.calendar.fragment.month.objects

import android.content.Context
import android.util.Log
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
        public const val EVENT_HEIGHT = 15.0F
        public const val TOP_MARGIN = 27.0F
        private const val LEFT_MARGIN = 1.0F
    }

    fun addEventUI(
            context: Context,
            name: String,
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
                MonthCalendarUIUtil.getEventView(context, name, false)
            }

            // 이틀 이상 이벤트 UI
            else -> {
                val eventView = TextView(context)

                eventView.textSize = MonthCalendarUIUtil.FONT_SIZE
                eventView.gravity = Gravity.CENTER_VERTICAL
                eventView.maxLines = 1
                eventView.text = name
                eventView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
                eventView.setTextColor(ContextCompat.getColor(context, R.color.invertFont))

                val startPadding = CalculatorUtil.dpToPx(2.0F)
                eventView.setPadding(startPadding, 0, 0, 0)

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

        val topMargin = CalculatorUtil.dpToPx(TOP_MARGIN + (order * (EVENT_HEIGHT + 2)))
        val leftMargin = CalculatorUtil.dpToPx(LEFT_MARGIN)

        set.connect(eventView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, topMargin)
        set.connect(eventView.id, ConstraintSet.START, startDayView.id, ConstraintSet.START, leftMargin)
        set.connect(eventView.id, ConstraintSet.END, endDayView.id, ConstraintSet.END, leftMargin)

        set.applyTo(weekLayout)
    }
}
