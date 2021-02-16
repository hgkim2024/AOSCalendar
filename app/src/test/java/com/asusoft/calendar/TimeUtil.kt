package com.asusoft.calendar

import android.icu.util.ChineseCalendar
import com.asusoft.calendar.util.calendarDay
import com.asusoft.calendar.util.calendarMonth
import com.asusoft.calendar.util.calendarYear
import com.asusoft.calendar.util.toStringDay
import java.text.SimpleDateFormat
import java.util.*


object TimeUtil {

    fun toString(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(date)
    }
}