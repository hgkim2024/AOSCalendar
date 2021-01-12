package com.asusoft.calendar

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

object TimeUtil {

    fun toString(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(date)
    }
}