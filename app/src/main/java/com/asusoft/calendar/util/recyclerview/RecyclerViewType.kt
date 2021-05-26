package com.asusoft.calendar.util.recyclerview

import com.asusoft.calendar.activity.addEvent.activity.ActivityAddEvent
import com.asusoft.calendar.activity.addEvent.activity.ActivityAddPerson
import com.asusoft.calendar.activity.calendar.activity.ActivityCalendar
import com.asusoft.calendar.activity.addEvent.dialog.DialogFragmentDaySelectCalendar
import com.asusoft.calendar.activity.calendar.dialog.filter.DialogFragmentFilter
import com.asusoft.calendar.activity.setting.fragment.FragmentMonthSetting
import com.asusoft.calendar.activity.setting.fragment.FragmentSetting
import com.asusoft.calendar.activity.calendar.fragment.month.FragmentMonthPage
import com.asusoft.calendar.activity.calendar.fragment.search.FragmentRecentSearchTerms
import com.asusoft.calendar.activity.calendar.fragment.search.FragmentEventSearchResult
import com.asusoft.calendar.activity.calendar.fragment.week.FragmentWeekPage
import com.asusoft.calendar.activity.setting.fragment.FragmentDaySetting
import com.asusoft.calendar.activity.setting.fragment.FragmentSettingFontSize

enum class RecyclerViewType(val value: Int) {
    ADD_EVENT(0),
    ONE_DAY_EVENT(1),
    SELECT_DAY(2),
    SIDE_MENU(3),
    VISIT_PERSON(4),
    RECENT_SEARCH(5),
    EVENT_SEARCH_RESULT(6),
    CALENDAR_SETTING(7);

    companion object {
        fun getType(typeObject: Any): RecyclerViewType {
            return when (typeObject) {
                is ActivityAddEvent -> ADD_EVENT
                is FragmentMonthPage,
                is FragmentWeekPage -> ONE_DAY_EVENT

                is DialogFragmentDaySelectCalendar -> SELECT_DAY
                is ActivityCalendar -> SIDE_MENU
                is ActivityAddPerson -> VISIT_PERSON
                is FragmentRecentSearchTerms -> RECENT_SEARCH
                is FragmentEventSearchResult -> EVENT_SEARCH_RESULT

                is FragmentMonthSetting,
                is FragmentDaySetting,
                is FragmentSettingFontSize,
                is FragmentSetting,
                is DialogFragmentFilter-> CALENDAR_SETTING

                else -> ADD_EVENT
            }
        }
    }
}