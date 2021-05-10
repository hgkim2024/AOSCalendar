package com.asusoft.calendar.activity.calendar

import android.graphics.drawable.Drawable
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication

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
            DAY -> "주"
        }
    }

    fun getIcon(): Drawable? {
        return when(this) {
            TOP -> null
            MONTH -> CalendarApplication.getDrawable(R.drawable.ic_baseline_view_module_24)
            DAY -> CalendarApplication.getDrawable(R.drawable.ic_baseline_view_list_24)
        }
    }

}