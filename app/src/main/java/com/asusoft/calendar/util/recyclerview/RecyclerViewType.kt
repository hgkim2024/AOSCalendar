package com.asusoft.calendar.util.recyclerview

import com.asusoft.calendar.activity.ActivityAddEvent
import com.asusoft.calendar.dialog.DialogFragmentDaySelectCalendar
import com.asusoft.calendar.fragment.month.FragmentMonthPage

enum class RecyclerViewType(val value: Int) {
    ADD_EVENT(0),
    ONE_DAY_EVENT(1),
    SELECT_DAY(2);

    companion object {
        fun getType(typeObject: Any): RecyclerViewType {
            return when (typeObject) {
                is ActivityAddEvent -> ADD_EVENT
                is FragmentMonthPage -> ONE_DAY_EVENT
                is DialogFragmentDaySelectCalendar -> SELECT_DAY
                else -> ADD_EVENT
            }
        }
    }
}