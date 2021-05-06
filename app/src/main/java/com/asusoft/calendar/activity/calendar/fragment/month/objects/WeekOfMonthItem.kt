package com.asusoft.calendar.activity.calendar.fragment.month.objects

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Point
import android.text.TextUtils
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.activity.calendar.fragment.month.enums.WeekOfDayType
import com.asusoft.calendar.util.objects.CalculatorUtil
import com.asusoft.calendar.util.objects.CalendarUtil.setCornerRadiusDrawable
import com.asusoft.calendar.util.objects.MonthCalendarUiUtil
import com.asusoft.calendar.util.objects.MonthCalendarUiUtil.COMPLETE_ALPHA
import com.asusoft.calendar.util.objects.MonthCalendarUiUtil.WEEK
import com.asusoft.calendar.util.objects.PreferenceKey
import com.asusoft.calendar.util.objects.PreferenceKey.DRAG_AND_DROP_DEFAULT
import com.asusoft.calendar.util.objects.PreferenceManager
import com.asusoft.calendar.util.endOfWeek
import com.asusoft.calendar.util.startOfWeek
import com.asusoft.calendar.util.weekOfDay
import java.util.*
import kotlin.collections.HashMap


class WeekOfMonthItem(
        val weekDate: Date,
        val rootLayout: ConstraintLayout,
        val weekLayout: ConstraintLayout,
        val dayViewList: ArrayList<TextView>,
        val eventViewList: HashMap<Int, HashMap<Int, View>> = HashMap<Int, HashMap<Int, View>>()
) {

    companion object {
        val TOP_MARGIN
            get() = CalculatorUtil.spToPx(MonthCalendarUiUtil.FONT_SIZE) + CalculatorUtil.dpToPx(13.0F)

        private const val LEFT_MARGIN = 1.5F
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addEventUI(
            context: Context,
            key: Long,
            name: String,
            startTime: Long,
            endTime: Long,
            order: Int,
            color: Int,
            isComplete: Boolean = true,
            isHoliday: Boolean = false
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
        val eventView = TextView(context)

        eventView.tag = key.toString()
        eventView.textSize = MonthCalendarUiUtil.FONT_SIZE - 1
//        eventView.gravity = Gravity.CENTER_VERTICAL
        eventView.setSingleLine()
        eventView.text = name
        eventView.ellipsize = TextUtils.TruncateAt.MARQUEE

        for (idx in startDay.value..endDay.value) {
            eventViewList[idx]?.set(order, eventView)
        }

        if (isHoliday) {
            eventView.setTextColor(CalendarApplication.getColor(R.color.invertFont))
        } else {
            if (PreferenceManager.getBoolean(PreferenceKey.MONTH_CALENDAR_DRAG_AND_DROP, DRAG_AND_DROP_DEFAULT)) {
                val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onLongPress(e: MotionEvent) {
                        onClick(eventView, e)
                    }
                })

                eventView.setOnTouchListener { v, event ->
//                onClick(v, event)
                    gestureDetector.onTouchEvent(event)
                    false
                }
            }

            eventView.setTextColor(CalendarApplication.getColor(R.color.invertFont))
        }

        val startPadding = CalculatorUtil.dpToPx(2.0F)
        eventView.setPadding(startPadding, CalculatorUtil.dpToPx(1.0F), 0, 0)
        setCornerRadiusDrawable(eventView, color)

        // UI 배치
        eventView.id = View.generateViewId()
        eventView.layoutParams = ConstraintLayout.LayoutParams(
                0,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        weekLayout.addView(eventView)

        val startDayView = dayViewList[startDay.value]
        val endDayView = dayViewList[endDay.value]

        var imageView: ImageView? = null

        if (isComplete) {
            val iv = ImageView(context)
            iv.id = View.generateViewId()
            iv.scaleType = ImageView.ScaleType.FIT_CENTER
            iv.setImageResource(R.drawable.ic_baseline_done_outline_24)
            iv.setColorFilter(color)
            iv.foregroundGravity = Gravity.END
            iv.bringToFront()
            eventView.alpha = COMPLETE_ALPHA

            iv.layoutParams = ConstraintLayout.LayoutParams(
                    0,
                    0
            )
            weekLayout.addView(iv)
            imageView = iv
        }

        val set = ConstraintSet()
        set.clone(weekLayout)

        val topMargin = TOP_MARGIN
        val leftMargin = CalculatorUtil.dpToPx(LEFT_MARGIN)
        val intervalMargin = CalculatorUtil.dpToPx(2.0F)

        set.connect(eventView.id, ConstraintSet.START, startDayView.id, ConstraintSet.START, leftMargin)
        set.connect(eventView.id, ConstraintSet.END, endDayView.id, ConstraintSet.END, leftMargin)

        if (order != 0) {
            for (idx in 0 until WEEK) {
                val view = eventViewList[idx]?.get(order - 1)

                if (view != null) {
                    set.connect(eventView.id, ConstraintSet.TOP, view.id, ConstraintSet.BOTTOM, intervalMargin)
                    break
                }
            }
        } else {
            set.connect(eventView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, topMargin)
        }

        if (isComplete
                && imageView != null) {
            set.setDimensionRatio(imageView.id, "1:1")
            set.connect(imageView.id, ConstraintSet.TOP, eventView.id, ConstraintSet.TOP)
            set.connect(imageView.id, ConstraintSet.BOTTOM, eventView.id, ConstraintSet.BOTTOM)
            set.connect(imageView.id, ConstraintSet.END, endDayView.id, ConstraintSet.END, leftMargin)
        }

        set.applyTo(weekLayout)
    }

    private fun onClick(v: View, event: MotionEvent): Boolean {
        if (!PreferenceManager.getBoolean(PreferenceKey.MONTH_CALENDAR_DRAG_AND_DROP, DRAG_AND_DROP_DEFAULT)) return false

        val x = event.x
        val y = event.y

        val item = ClipData.Item(v.tag as CharSequence)
        val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
        val data = ClipData(v.tag.toString(), mimeTypes, item)
        val dragShadow: View.DragShadowBuilder = object : View.DragShadowBuilder(v) {

            override fun onProvideShadowMetrics(shadowSize: Point, shadowTouchPoint: Point) {
                shadowSize.set(v.width, v.height)
                shadowTouchPoint.set(x.toInt(), y.toInt())
            }
        }

        v.startDragAndDrop(
                data,
                dragShadow,
                v,
                0
        )

        return true
    }
}
