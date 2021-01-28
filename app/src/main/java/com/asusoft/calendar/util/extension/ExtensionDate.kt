package com.asusoft.calendar.util

import com.asusoft.calendar.fragment.month.enums.WeekOfDayType
import java.text.SimpleDateFormat
import java.util.*

/*
*
날짜 반환
*/
val Date.calendarDay: Int
    get() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = this
        return calendar.get(Calendar.DAY_OF_MONTH)
    }


/*
*
요일 반환
* 일요일: 0, 월요일: 1, ... , 토요일: 6
*/
val Date.weekOfDay: Int
    get() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = this
        return calendar.get(Calendar.DAY_OF_WEEK) - 1
    }

/*
*
달 반환
*/
val Date.calendarMonth: Int
    get() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = this
        return calendar.get(Calendar.MONTH) + 1
    }


/*
*
해당 날의 시작 시간을 반환
*/
val Date.startOfDay: Date
    get() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = this
        return getStartOfDay(calendar)
    }


/*
*
해당 날의 마지막 시간을 반환
*/
val Date.endOfDay: Date
    get() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = this
        return getEndOfDay(calendar)
    }


/*
*
해당 주의 첫 날(일요일)을 반환
*/
val Date.startOfWeek: Date
    get() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = this
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        return getStartOfDay(calendar)
    }


/*
*
해당 주의 마지막 날(토요일)을 반환
*/
val Date.endOfWeek: Date
    get() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = nextWeek
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        return calendar.time.yesterday.endOfDay
    }


/*
*
해당 달의 첫 날을 반환
*/
val Date.startOfMonth: Date
    get() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = this
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return getStartOfDay(calendar)
    }


/*
*
해당 달의 마지막 날을 반환
*/
val Date.endOfMonth: Date
    get() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = nextMonth
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.time.yesterday.endOfDay
    }


/*
*
해당 날의 다음 날을 반환
*/
val Date.tomorrow: Date
    get() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = this
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return getStartOfDay(calendar)
    }


/*
*
해당 날의 이전 날을 반환
*/
val Date.yesterday: Date
    get() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = this
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return getStartOfDay(calendar)
    }


/*
*
해당 날을 기준으로 다음 주 반환
*/
val Date.nextWeek: Date
    get() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = this
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        return getStartOfDay(calendar)
    }


/*
*
해당 날을 기준으로 이전 주 반환
*/
val Date.prevWeek: Date
    get() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = this
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
        return getStartOfDay(calendar)
    }


/*
*
해당 날을 기준으로 다음 달 반환
*/
val Date.nextMonth: Date
    get() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = this
        calendar.add(Calendar.MONTH, 1)
        return getStartOfDay(calendar)
    }


/*
*
해당 날을 기준으로 이전 달 반환
*/
val Date.prevMonth: Date
    get() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = this
        calendar.add(Calendar.MONTH, -1)
        return getStartOfDay(calendar)
    }

/*
*
해당 날의 count 만큼 이동한 날을 반환
*/
fun Date.getNextDay(count: Int): Date {
    val calendar: Calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.DAY_OF_YEAR, count)
    return getStartOfDay(calendar)
}


/*
*
해당 달의 count 만큼 이동한 달을 반환
*/
fun Date.getNextMonth(count: Int): Date {
    val calendar: Calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.MONTH, count)
    return getStartOfDay(calendar)
}


/*
*
해당 날의 시작 시간을 반환
*/
fun Date.getStartOfDay(calendar: Calendar): Date {
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.time
}


/*
*
해당 날의 마지막 시간을 반환
*/
fun Date.getEndOfDay(calendar: Calendar): Date {
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, -1);
    return calendar.time
}


/*
*
오늘 반환
*/
fun Date.getToday(): Date {
    val calendar: Calendar = Calendar.getInstance()
    return getStartOfDay(calendar)
}


/*
*
* Date 를 0000년 00월 00일 String 으로 변환
*/
fun Date.toStringDay(): String {
    val sdf = SimpleDateFormat("yyyy년 MM월 dd일")
    var dateString = sdf.format(this)
    dateString += "(${WeekOfDayType.fromInt(weekOfDay).getShortTitle()})"
    return dateString
}


/*

* yyyy-MM-dd String 을 Date 로 변환
*/
fun Date.stringToDate(dateString: String): Date {
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    return sdf.parse(dateString)
}