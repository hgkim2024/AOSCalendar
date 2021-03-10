package com.asusoft.calendar.util.recyclerview

import com.asusoft.calendar.activity.ActivityAddEvent
import com.asusoft.calendar.activity.start.ActivityStart
import com.asusoft.calendar.dialog.DialogFragmentDaySelectCalendar
import com.asusoft.calendar.fragment.day.FragmentDayCalendar
import com.asusoft.calendar.fragment.month.FragmentMonthPage
import com.asusoft.calendar.util.recyclerview.holder.dayevent.header.DayCalendarHeaderHolder

enum class RecyclerViewType(val value: Int) {
    ADD_EVENT(0),
    ONE_DAY_EVENT(1),
    SELECT_DAY(2),
    DAY_CALENDAR_HEADER(3),
    DAY_CALENDAR_BODY(4),
    SIDE_MENU(5);

    companion object {
        fun getType(typeObject: Any): RecyclerViewType {
            return when (typeObject) {
                is ActivityAddEvent -> ADD_EVENT
                is FragmentMonthPage -> ONE_DAY_EVENT
                is DialogFragmentDaySelectCalendar -> SELECT_DAY
                is FragmentDayCalendar -> DAY_CALENDAR_HEADER
                is DayCalendarHeaderHolder -> DAY_CALENDAR_BODY
                is ActivityStart -> SIDE_MENU
                else -> ADD_EVENT
            }
        }
    }
}