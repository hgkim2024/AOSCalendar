package com.asusoft.calendar.util.recyclerview

import com.asusoft.calendar.activity.addEvent.activity.ActivityAddEvent
import com.asusoft.calendar.activity.addEvent.activity.ActivityAddPerson
import com.asusoft.calendar.activity.calendar.activity.ActivityCalendar
import com.asusoft.calendar.activity.addEvent.dialog.DialogFragmentDaySelectCalendar
import com.asusoft.calendar.activity.setting.fragment.FragmentMonthSetting
import com.asusoft.calendar.activity.setting.fragment.FragmentSetting
import com.asusoft.calendar.activity.calendar.fragment.day.FragmentDayCalendar
import com.asusoft.calendar.activity.calendar.fragment.month.FragmentMonthPage
import com.asusoft.calendar.activity.calendar.fragment.search.FragmentRecentSearchTerms
import com.asusoft.calendar.activity.calendar.fragment.search.FragmentEventSearchResult
import com.asusoft.calendar.activity.setting.fragment.FragmentDaySetting
import com.asusoft.calendar.util.recyclerview.holder.calendar.dayevent.header.DayCalendarHeaderHolder

enum class RecyclerViewType(val value: Int) {
    ADD_EVENT(0),
    ONE_DAY_EVENT(1),
    SELECT_DAY(2),
    DAY_CALENDAR_HEADER(3),
    DAY_CALENDAR_BODY(4),
    SIDE_MENU(5),
    VISIT_PERSON(6),
    RECENT_SEARCH(7),
    EVENT_SEARCH_RESULT(8),
    CALENDAR_SETTING(9);

    companion object {
        fun getType(typeObject: Any): RecyclerViewType {
            return when (typeObject) {
                is ActivityAddEvent -> ADD_EVENT
                is FragmentMonthPage -> ONE_DAY_EVENT
                is DialogFragmentDaySelectCalendar -> SELECT_DAY
                is FragmentDayCalendar -> DAY_CALENDAR_HEADER
                is DayCalendarHeaderHolder -> DAY_CALENDAR_BODY
                is ActivityCalendar -> SIDE_MENU
                is ActivityAddPerson -> VISIT_PERSON
                is FragmentRecentSearchTerms -> RECENT_SEARCH
                is FragmentEventSearchResult -> EVENT_SEARCH_RESULT
                is FragmentSetting -> SIDE_MENU
                is FragmentMonthSetting -> CALENDAR_SETTING
                is FragmentDaySetting -> CALENDAR_SETTING
                else -> ADD_EVENT
            }
        }
    }
}