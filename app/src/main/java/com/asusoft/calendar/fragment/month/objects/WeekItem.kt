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
import com.asusoft.calendar.fragment.month.WeekOfDayType
import com.asusoft.calendar.util.`object`.CalculatorUtil
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.endOfWeek
import com.asusoft.calendar.util.startOfWeek
import com.asusoft.calendar.util.weekOfDay
import java.util.*


class WeekItem(val weekDate: Date, val weekLayout: ConstraintLayout, val dayViewList: ArrayList<View>) {

    private final val EVENT_HEIGHT = 17.0F
    private final val TOP_MARGIN = 27.0F
    private final val LEFT_MARGIN = 2.0F

    fun addEventUI(
            context: Context,
            startTime: Long,
            endTime: Long,
            isOneDay: Boolean,
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

        if (endDay.value < startDay.value) return

        // UI 생성
        val eventView: View = when {

            // 하루 이벤트 UI
            isOneDay -> {
                val inflater = LayoutInflater.from(context)
                val eventView = inflater.inflate(R.layout.view_monthly_one_day_event, null, false)
                eventView
            }

            // 이틀 이상 이벤트 UI
            else -> {
                val eventView = TextView(context)

                eventView.textSize = MonthCalendarUIUtil.FONT_SIZE
                eventView.gravity = Gravity.CENTER_VERTICAL
                eventView.maxLines = 1
                eventView.text = "이벤트"
                eventView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
                eventView.setTextColor(ContextCompat.getColor(context, R.color.invertFont))
                eventView
            }
        }

        // UI 배치
        eventView.id = View.generateViewId()

        eventView.layoutParams = ConstraintLayout.LayoutParams(
                0,
                CalculatorUtil.dpToPx(context, EVENT_HEIGHT)
        )
        weekLayout.addView(eventView)

        val startDayView = dayViewList[startDay.getIndex()]
        val endDayView = dayViewList[endDay.getIndex()]

        val set = ConstraintSet()
        set.clone(weekLayout)

        val padding = CalculatorUtil.dpToPx(context, LEFT_MARGIN)
        eventView.setPadding(padding, 0, padding, 0)

        val topMargin = CalculatorUtil.dpToPx(context, TOP_MARGIN + (order * (EVENT_HEIGHT + 2)))
        val leftMargin = CalculatorUtil.dpToPx(context, LEFT_MARGIN)

        set.connect(eventView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, topMargin)
        set.connect(eventView.id, ConstraintSet.START, startDayView.id, ConstraintSet.START, leftMargin)
        set.connect(eventView.id, ConstraintSet.END, endDayView.id, ConstraintSet.END, leftMargin)

        set.applyTo(weekLayout)
    }
}
