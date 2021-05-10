package com.asusoft.calendar.util.objects

import android.graphics.drawable.GradientDrawable
import android.view.View
import com.asusoft.calendar.activity.addEvent.activity.ActivityAddEvent
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.holiday.LunarCalendar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object CalendarUtil {
    val DAY_FONT_SIZE: Float
        get() = PreferenceManager.getInt(PreferenceKey.DAY_CALENDAR_FONT_SIZE, PreferenceKey.DAY_DEFAULT_FONT_SIZE.toInt()).toFloat()

    fun getDayEventList(date: Date, isHoliday: Boolean = true): ArrayList<Any> {
        val eventList: ArrayList<Any> = ArrayList()
        val eventDayList = RealmEventDay.getOneDayCopyList(date)
        val oneDayCopyList = ArrayList(eventDayList.filter { it.startTime == it.endTime })
        val multiDayCopyList = ArrayList(eventDayList.filter { it.startTime != it.endTime })
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

        val size = oneDayCopyList.size + multiDayCopyList.size + 5

        while (
            (oneDayCopyList.isNotEmpty()
                    || multiDayCopyList.isNotEmpty())
            || order < size
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
        val eventDayList = RealmEventDay.selectOneWeek(weekDate.startOfWeek)
        val multiDayList = eventDayList.filter { it.startTime != it.endTime }
        val oneDayList = eventDayList.filter { it.startTime == it.endTime }

        return getEventOrderList(
            weekDate,
            multiDayList,
            oneDayList
        )
    }


    fun getEventOrderList(
        weekDate: Date,
        realmEventDayList: List<RealmEventDay>,
        realmEventOneDayList: List<RealmEventDay>,
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
            realmEventDayList.filter { !it.isComplete },
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
            realmEventDayList.filter { it.isComplete },
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
        realmEventDayList: List<RealmEventDay>,
        dayCheckList: ArrayList<Array<Boolean>>,
        orderMap: HashMap<Long, Int>
    ) {
        for (eventMultiDay in realmEventDayList) {
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
                if (startOfWeek > endOfWeek) break@loop

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
        realmEventOneDayList: List<RealmEventDay>,
        dayCheckList: ArrayList<Array<Boolean>>,
        orderMap: HashMap<Long, Int>
    ) {
        for (eventOneDay in realmEventOneDayList) {
            val weekOfDay = Date(eventOneDay.startTime).weekOfDay

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

    fun setCornerRadiusDrawable(v: View, backgroundColor: Int, r: Float = 2.0F) {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        val r = CalculatorUtil.dpToPx(r).toFloat()
        shape.cornerRadii = floatArrayOf(r, r, r, r, r, r, r, r)
        shape.setColor(backgroundColor)
//        shape.setStroke(3, borderColor)
        v.background = shape
    }

    fun setLeftCornerRadiusDrawable(v: View, backgroundColor: Int) {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        val r = CalculatorUtil.dpToPx(2.0F).toFloat()
        shape.cornerRadii = floatArrayOf(r, r, 0F, 0F, 0F, 0F, r, r)
        shape.setColor(backgroundColor)
        v.background = shape
    }

    fun calendarRefresh(isRemoveDayEventView: Boolean = false) {
        val event = HashMapEvent(java.util.HashMap())
        event.map[ActivityAddEvent.toStringActivity()] = ActivityAddEvent.toStringActivity()

        if (isRemoveDayEventView) {
            event.map["removeDayEventView"] = "removeDayEventView"
        }

        GlobalBus.post(event)
    }
}