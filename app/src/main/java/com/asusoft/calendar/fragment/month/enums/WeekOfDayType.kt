package com.asusoft.calendar.fragment.month.enums

import android.content.Context
import androidx.core.content.ContextCompat
import com.asusoft.calendar.R

enum class WeekOfDayType(val value: Int) {
    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }

    fun getShortTitle(): String {
        return when(this) {
            SUNDAY -> "일"
            MONDAY -> "월"
            TUESDAY -> "화"
            WEDNESDAY -> "수"
            THURSDAY -> "목"
            FRIDAY -> "금"
            SATURDAY -> "토"
        }
    }

    fun getTitle(): String {
        return getShortTitle() + "요일"
    }

    fun getFontColor(context: Context): Int {
        return when(this) {
            SUNDAY -> ContextCompat.getColor(context, R.color.holiday)
            SATURDAY -> ContextCompat.getColor(context, R.color.saturday)
            else -> ContextCompat.getColor(context, R.color.font)
        }
    }

}