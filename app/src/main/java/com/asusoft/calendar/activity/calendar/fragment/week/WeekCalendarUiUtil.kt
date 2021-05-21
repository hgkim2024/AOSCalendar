package com.asusoft.calendar.activity.calendar.fragment.week

import android.content.ClipDescription
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.util.Log
import android.view.DragEvent
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
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.removeFromSuperView
import com.asusoft.calendar.util.holiday.LunarCalendar
import com.asusoft.calendar.util.objects.CalendarUtil
import com.asusoft.calendar.util.objects.PreferenceKey
import com.asusoft.calendar.util.objects.PreferenceManager
import com.asusoft.calendar.util.objects.ThemeUtil
import java.util.*
import kotlin.collections.ArrayList

object WeekCalendarUiUtil {

    public const val WEEK = 7
    private const val WEIGHT_SUM = 100.0F
    public const val COMPLETE_ALPHA = 0.4F

    private val HEADER_FONT_SIZE: Float
        get() = PreferenceManager.getFloat(PreferenceKey.WEEK_CALENDAR_HEADER_FONT_SIZE, PreferenceKey.WEEK_CALENDAR_HEADER_DEFAULT_FONT_SIZE)

    public val ITEM_FONT_SIZE: Float
        get() = PreferenceManager.getFloat(PreferenceKey.WEEK_CALENDAR_EVENT_FONT_SIZE, PreferenceKey.WEEK_CALENDAR_EVENT_DEFAULT_FONT_SIZE)

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
        val emptyView = TextView(context)
        weekLayout.addView(emptyView)
        emptyView.id = View.generateViewId()
        emptyView.tag = "tv_empty"
        emptyView.text = "등록된 이벤트가 없습니다."
        emptyView.setTextColor(ThemeUtil.instance.lightFont)

        weekLayout.layoutParams = FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )

        emptyView.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        for(idx in 0 until WEEK) {
            val vw = View(context)
            vw.id = View.generateViewId()
            weekLayout.addView(vw)
            vw.setOnDragListener { v, event ->
                onDrag(v, event)
            }

            vw.tag = date.time
            vw.setBackgroundColor(ThemeUtil.instance.background)

            vw.layoutParams = ConstraintLayout.LayoutParams(
                    0,
                    0
            )
            dayViewList.add(vw)

            date = date.tomorrow
        }

        emptyView.bringToFront()

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

        set.connect(emptyView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(emptyView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        set.connect(emptyView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        set.connect(emptyView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

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
                        ThemeUtil.instance.holiday,
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

            tv.text = days[idx].getShortTitle()
            tv.setTextColor(days[idx].getFontColor())

            tv.textSize = HEADER_FONT_SIZE
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
            weekItem: WeekItem,
            prevDayEventView: ConstraintLayout?
    ) {
        val removeViewList = ArrayList<View>()
        val tvEmpty = weekItem.weekLayout.findViewWithTag<TextView?>("tv_empty") ?: return

        for (idx in 0 until weekItem.weekLayout.childCount) {
            val v = weekItem.weekLayout.getChildAt(idx)

            if (!weekItem.dayViewList.contains(v) && v != prevDayEventView && v != tvEmpty) {
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

//        Logger.d("refreshPage date: ${weekItem.weekDate.toStringDay()}")
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

                if (FragmentWeekPage.dragInitFlag) {
                    FragmentWeekPage.dragInitFlag = false

                    val event = HashMapEvent(HashMap())
                    event.map[WeekCalendarUiUtil.toString()] = WeekCalendarUiUtil.toString()
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

                val event = HashMapEvent(HashMap())
                event.map[WeekCalendarUiUtil.toString()] = WeekCalendarUiUtil.toString()
                event.map["removeDayEventView"] = "removeDayEventView"
                GlobalBus.post(event)

                return true
            }

            DragEvent.ACTION_DROP -> {
                v.background.clearColorFilter()
                v.invalidate()

                val vw = event.localState as? View

                if (vw != null) {
                    if (!FragmentWeekPage.dragInitFlag) {
                        val eventMap = HashMapEvent(HashMap())
                        eventMap.map[WeekCalendarUiUtil.toString()] = WeekCalendarUiUtil.toString()
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