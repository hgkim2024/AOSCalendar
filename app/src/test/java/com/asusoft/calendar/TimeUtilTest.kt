package com.asusoft.calendar

import com.asusoft.calendar.util.*
import org.junit.Test
import java.util.*

class TimeUtilTest {

    @Test
    fun getDay() {
        val today = Date().getToday()
        println("day: ${today.calendarDay}")
        println("month: ${today.calendarMonth}")

        val str = TimeUtil.toString(Date(today.time))
        println(str)
    }

    @Test
    fun getWeekOfDay() {
        var today = Date().getToday()
        println("===============================")
        println("weekOfDay: ${today.weekOfDay}")
        val str = TimeUtil.toString(today)
        println("time: $str")

        for(i in 0 until 7) {
            today = today.tomorrow
            println("===============================")
            println("weekOfDay: ${today.weekOfDay}")
            val str = TimeUtil.toString(today)
            println("time: $str")
        }
    }

    @Test
    fun getMonth() {
        var today = Date().getToday().startOfMonth
        println("month: ${today.calendarMonth}")

        for(i in 0..30) {
            today = today.nextMonth
            println("month: ${today.calendarMonth}")
        }
    }

    @Test
    fun today() {
        val today = Date().getToday()
        println("today: $today")
    }

    @Test
    fun tomorrowDate() {
        val today = Date().getToday()
        val tomorrow = today.tomorrow
        val str = TimeUtil.toString(tomorrow)
        println("tomorrow: $str")
    }

    @Test
    fun yesterdayDate() {
        val today = Date().getToday()
        val yesterday = today.yesterday
        val str = TimeUtil.toString(yesterday)
        println("yesterday: $str")
    }

    @Test
    fun startOfWeek() {
        val today = Date().getToday()
        val str = TimeUtil.toString(today.startOfWeek)
        println("startOfWeek: $str")
    }

    @Test
    fun endOfWeek() {
        val today = Date().getToday()
        val str = TimeUtil.toString(today.endOfWeek)
        println("startOfWeek: $str")
    }

    @Test
    fun nextWeek() {
        val today = Date().getToday()
        val str = TimeUtil.toString(today.startOfWeek.nextWeek)
        println("startOfWeek: $str")
    }

    @Test
    fun prevWeek() {
        val today = Date().getToday()
        val str = TimeUtil.toString(today.startOfWeek.prevWeek)
        println("startOfWeek: $str")
    }

    @Test
    fun startOfMonth() {
        val today = Date().getToday()
        val str = TimeUtil.toString(today.startOfMonth)
        println("startOfMonth: $str")
    }

    @Test
    fun endOfMonth() {
        val today = Date().getToday()
        val str = TimeUtil.toString(today.endOfMonth)
        println("endOfMonth: $str")
    }

    @Test
    fun nextMonth() {
        val today = Date().getToday()
        val str = TimeUtil.toString(today.startOfMonth.nextMonth)
        println("startOfWeek: $str")
    }

    @Test
    fun prevMonth() {
        val today = Date().getToday()
        val str = TimeUtil.toString(today.startOfMonth.prevMonth)
        println("startOfWeek: $str")
    }

    @Test
    fun movePage() {
        val moveDate = Date().getToday()
        val curPageDate = moveDate.getNextDay(-14)
        val diff = ((moveDate.time - curPageDate.time) / 1000 / 60 / 60 / 24 / 7).toInt()
        println("diff: $diff")
    }
}