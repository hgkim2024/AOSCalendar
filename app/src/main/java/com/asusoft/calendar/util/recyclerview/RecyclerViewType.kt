package com.asusoft.calendar.util.recyclerview

import com.asusoft.calendar.activity.ActivityAddEvent

enum class RecyclerViewType(val value: Int) {
    ADD_EVENT(0);

    companion object {
        fun getType(typeObject: Any): RecyclerViewType {
            return when (typeObject) {
                is ActivityAddEvent -> ADD_EVENT
                else -> ADD_EVENT
            }
        }
    }
}