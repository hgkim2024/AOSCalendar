package com.asusoft.calendar.fragment.month.objects

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
import com.asusoft.calendar.fragment.month.enums.WeekOfDayType
import com.asusoft.calendar.util.`object`.CalculatorUtil
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil.COMPLETE_ALPHA
import com.asusoft.calendar.util.endOfWeek
import com.asusoft.calendar.util.startOfWeek
import com.asusoft.calendar.util.weekOfDay
import java.util.*


class WeekItem(
        val weekDate: Date,
        val rootLayout: ConstraintLayout,
        val weekLayout: ConstraintLayout,
        val dayViewList: ArrayList<TextView>
) {

    companion object {
        public const val EVENT_HEIGHT = 15.0F
        public const val TOP_MARGIN = 27.0F
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
        eventView.textSize = MonthCalendarUIUtil.FONT_SIZE - 1
        eventView.gravity = Gravity.CENTER_VERTICAL
        eventView.setSingleLine()
        eventView.text = name
        eventView.ellipsize = TextUtils.TruncateAt.MARQUEE

        // TODO: - 터치 이벤트 중첩되는 방법 알아보기
        eventView.setOnTouchListener { v, event -> false }


        if (isHoliday) {
            MonthCalendarUIUtil.setCornerRadiusDrawable(eventView, CalendarApplication.getColor(R.color.holidayBackground))
            eventView.setTextColor(CalendarApplication.getColor(R.color.invertFont))
        } else {
//            eventView.setOnLongClickListener(this)

//            val longClick = LongPressChecker(context)
//            longClick.setOnLongPressListener(object : LongPressChecker.OnLongPressListener {
//                override fun onLongPressed(x: Float, y: Float) {
//                    onLongClick(eventView, x, y)
//                }
//            })

            val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onLongPress(e: MotionEvent) {
                    onLongClick(eventView, e.x, e.y)
                }
            })

            eventView.setOnTouchListener { v, event ->
                gestureDetector.onTouchEvent(event)
                false
            }

            MonthCalendarUIUtil.setCornerRadiusDrawable(eventView, CalendarApplication.getColor(R.color.colorAccent))
            eventView.setTextColor(CalendarApplication.getColor(R.color.invertFont))
        }

        val startPadding = CalculatorUtil.dpToPx(2.0F)
        eventView.setPadding(startPadding, 0, 0, 0)

        // UI 배치
        eventView.id = View.generateViewId()

        eventView.layoutParams = ConstraintLayout.LayoutParams(
                0,
                CalculatorUtil.dpToPx(EVENT_HEIGHT)
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
            iv.setColorFilter(CalendarApplication.getColor(R.color.colorAccent))
            iv.bringToFront()
            eventView.alpha = COMPLETE_ALPHA

            iv.layoutParams = ConstraintLayout.LayoutParams(
                    CalculatorUtil.dpToPx(EVENT_HEIGHT),
                    CalculatorUtil.dpToPx(EVENT_HEIGHT)
            )
            weekLayout.addView(iv)
            imageView = iv
        }

        val set = ConstraintSet()
        set.clone(weekLayout)

        val topMargin = CalculatorUtil.dpToPx(TOP_MARGIN + (order * (EVENT_HEIGHT + 2)))
        val leftMargin = CalculatorUtil.dpToPx(LEFT_MARGIN)

        set.connect(eventView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, topMargin)
        set.connect(eventView.id, ConstraintSet.START, startDayView.id, ConstraintSet.START, leftMargin)
        set.connect(eventView.id, ConstraintSet.END, endDayView.id, ConstraintSet.END, leftMargin)

        if (isComplete
                && imageView != null) {
            set.connect(imageView.id, ConstraintSet.TOP, eventView.id, ConstraintSet.TOP)
            set.connect(imageView.id, ConstraintSet.BOTTOM, eventView.id, ConstraintSet.BOTTOM)
            set.connect(imageView.id, ConstraintSet.END, endDayView.id, ConstraintSet.END, leftMargin)
        }

        set.applyTo(weekLayout)
    }

    fun onLongClick(v: View, x: Float, y: Float): Boolean {
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
