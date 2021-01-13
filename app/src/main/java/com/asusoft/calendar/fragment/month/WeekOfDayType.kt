package com.asusoft.calendar.fragment.month

import android.content.Context
import androidx.core.content.ContextCompat
import com.asusoft.calendar.R

enum class WeekOfDayType(val value: Int) {
    SUNDAY(1),
    MONDAY(2),
    TUESDAY(3),
    WEDNESDAY(4),
    THURSDAY(5),
    FRIDAY(6),
    SATURDAY(7);

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
            SUNDAY -> ContextCompat.getColor(context, R.color.red)
            SATURDAY -> ContextCompat.getColor(context, R.color.blue)
            else -> ContextCompat.getColor(context, R.color.font)
        }
    }

}