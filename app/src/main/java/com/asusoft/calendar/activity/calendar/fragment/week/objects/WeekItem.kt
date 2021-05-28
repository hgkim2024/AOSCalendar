package com.asusoft.calendar.activity.calendar.fragment.week.objects

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Point
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.HorizontalScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.fragment.month.MonthCalendarUiUtil
import com.asusoft.calendar.activity.calendar.fragment.month.enums.WeekOfDayType
import com.asusoft.calendar.activity.calendar.fragment.week.WeekCalendarUiUtil
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.util.endOfWeek
import com.asusoft.calendar.util.objects.*
import com.asusoft.calendar.util.startOfWeek
import com.asusoft.calendar.util.weekOfDay
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.*
import kotlin.collections.HashMap

class WeekItem(
        val weekDate: Date,
        val rootLayout: HorizontalScrollView,
        val weekLayout: ConstraintLayout,
        val dayViewList: ArrayList<View>,
        val eventViewList: HashMap<Int, HashMap<Int, View>> = HashMap()
) {

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
        val eventView = WeekCalendarUiUtil.getOneDayView(context)

        val tv = eventView.findViewById<TextView>(R.id.tv)
        
        eventView.tag = key.toString()
        tv.textSize = WeekCalendarUiUtil.ITEM_FONT_SIZE
        tv.text = name
//        tv.ellipsize = TextUtils.TruncateAt.MARQUEE

        for (idx in startDay.value..endDay.value) {
            eventViewList[idx]?.set(order, eventView)
        }

        tv.setTextColor(ThemeUtil.instance.eventFontColor)

        val checkbox = eventView.findViewById<CheckBox>(R.id.checkbox)
        checkbox.isChecked = isComplete

        if (isComplete) {
            eventView.alpha = WeekCalendarUiUtil.COMPLETE_ALPHA
        }

        checkbox.clicks()
//                .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
//                    Logger.d("checkbox: ${checkbox.isChecked}")

                    val event = RealmEventDay.selectOne(key)
                    event?.update(
                            name,
                            startTime,
                            endTime,
                            checkbox.isChecked
                    )

                    CalendarUtil.calendarRefresh()
                }

        if (isHoliday) {
            checkbox.visibility = View.GONE
        }

        if (!isHoliday) {
            if (PreferenceManager.getBoolean(PreferenceKey.MONTH_CALENDAR_DRAG_AND_DROP, PreferenceKey.DRAG_AND_DROP_DEFAULT)) {
                val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onLongPress(e: MotionEvent) {
                        onClick(eventView, e)
                    }
                })

                eventView.setOnTouchListener { v, event ->
                    gestureDetector.onTouchEvent(event)
                    false
                }
            }
        }

        val startPadding = CalculatorUtil.dpToPx(2.0F)
        eventView.setPadding(startPadding, CalculatorUtil.dpToPx(1.0F), 0, 0)
        CalendarUtil.setCornerRadiusDrawable(eventView, color, 5.0F)

        // UI 배치
        eventView.id = View.generateViewId()
        eventView.layoutParams = ConstraintLayout.LayoutParams(
                CalculatorUtil.getDeviceWidth() * 18 / 100,
                0
        )
        weekLayout.addView(eventView)

        val startDayView = dayViewList[startDay.value]
        val endDayView = dayViewList[endDay.value]

        val set = ConstraintSet()
        set.clone(weekLayout)

        val margin = CalculatorUtil.dpToPx(2.0F)
        set.connect(eventView.id, ConstraintSet.TOP, startDayView.id, ConstraintSet.TOP, margin)
        set.connect(eventView.id, ConstraintSet.BOTTOM, endDayView.id, ConstraintSet.BOTTOM, margin)

        if (order != 0) {
            for (idx in 0 until MonthCalendarUiUtil.WEEK) {
                val view = eventViewList[idx]?.get(order - 1)

                if (view != null) {
                    set.connect(eventView.id, ConstraintSet.START, view.id, ConstraintSet.END, margin * 2)
                    break
                }
            }
        } else {
            set.connect(eventView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, margin)
        }

        set.applyTo(weekLayout)
    }

    private fun onClick(v: View, event: MotionEvent): Boolean {
        if (!PreferenceManager.getBoolean(PreferenceKey.MONTH_CALENDAR_DRAG_AND_DROP, PreferenceKey.DRAG_AND_DROP_DEFAULT)) return false

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