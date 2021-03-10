package com.asusoft.calendar.activity.start

enum class SideMenuType(val value: Int) {
    TOP(0),
    MONTH(1),
    DAY(2);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }

    fun getTitle(): String {
        return when(this) {
            TOP -> ""
            MONTH -> "월"
            DAY -> "일"
        }
    }

}