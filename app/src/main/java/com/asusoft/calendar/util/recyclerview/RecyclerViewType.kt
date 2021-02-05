package com.asusoft.calendar.util.recyclerview

import com.asusoft.calendar.activity.ActivityAddEvent
import com.asusoft.calendar.fragment.month.FragmentMonthPage

enum class RecyclerViewType(val value: Int) {
    ADD_EVENT(0),
    ONE_DAY_EVENT(1);

    companion object {
        fun getType(typeObject: Any): RecyclerViewType {
            return when (typeObject) {
                is ActivityAddEvent -> ADD_EVENT
                is FragmentMonthPage -> ONE_DAY_EVENT
                else -> ADD_EVENT
            }
        }
    }
}