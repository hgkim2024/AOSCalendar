package com.asusoft.calendar.activity.calendar.dialog.filter.enums

enum class DateFilterType(val value: Int) {
    ALL(0),
    MONTH_1(1),
    MONTH_3(2),
    MONTH_6(3);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }

    fun getTitle(): String {
        return when(this) {
            ALL -> "전체"
            MONTH_1 -> "1개월"
            MONTH_3 -> "3개월"
            MONTH_6 -> "6개월"
        }
    }
}