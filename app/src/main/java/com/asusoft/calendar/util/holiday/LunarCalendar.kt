package com.asusoft.calendar.util.holiday

import android.icu.util.ChineseCalendar
import com.asusoft.calendar.util.getNextDay
import com.asusoft.calendar.util.stringToDate_yyyyMMdd
import com.asusoft.calendar.util.toString_yyyyMMdd
import com.orhanobut.logger.Logger
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

object LunarCalendar {
    private var holidaysArrayList = Collections.synchronizedList(ArrayList<Holiday>())
    var holidaysMap = ConcurrentHashMap<String, List<Holiday>>()

    /**
     * 음력날짜를 양력날짜로 변환
     * @param 음력날짜 (yyyyMMdd)
     * @return 양력날짜 (yyyyMMdd)
     */
    private fun lunarToSolar(yyyymmdd: String?): String {
        val cc = ChineseCalendar()
        val cal = Calendar.getInstance()

        if (yyyymmdd == null) return ""

        var date = yyyymmdd
        if (date.length != 8) {
            date =
                    when {
                        date.length == 4 -> date + "0101"
                        date.length == 6 -> date + "01"
                        date.length > 8 -> date.substring(0, 8)
                        else -> return ""
                    }
        }

        cc[ChineseCalendar.EXTENDED_YEAR] = date.substring(0, 4).toInt() + 2637
        cc[ChineseCalendar.MONTH] = date.substring(4, 6).toInt() - 1
        cc[ChineseCalendar.DAY_OF_MONTH] = date.substring(6).toInt()

        cal.timeInMillis = cc.timeInMillis
        val y = cal[Calendar.YEAR]
        val m = cal[Calendar.MONTH] + 1
        val d = cal[Calendar.DAY_OF_MONTH]

        val ret = StringBuffer()
        ret.append(String.format("%04d", y))
        ret.append(String.format("%02d", m))
        ret.append(String.format("%02d", d))
        return ret.toString()
    }

    /**
     * 양력날짜를 음력날짜로 변환
     * @param 양력날짜 (yyyyMMdd)
     * @return 음력날짜 (yyyyMMdd)
     */
    fun solarToLunar(yyyymmdd: String?): String {
        val cc = ChineseCalendar()
        val cal = Calendar.getInstance()

        if (yyyymmdd == null) return ""

        var date = yyyymmdd
        if (date.length != 8) {
            date =
                    when {
                        date.length == 4 -> date + "0101"
                        date.length == 6 -> date + "01"
                        date.length > 8 -> date.substring(0, 8)
                        else -> return ""
                    }
        }
        cal[Calendar.YEAR] = date.substring(0, 4).toInt()
        cal[Calendar.MONTH] = date.substring(4, 6).toInt() - 1
        cal[Calendar.DAY_OF_MONTH] = date.substring(6).toInt()
        cc.timeInMillis = cal.timeInMillis

        // ChinessCalendar.YEAR 는 1~60 까지의 값만 가지고 ,
        // ChinessCalendar.EXTENDED_YEAR 는 Calendar.YEAR 값과 2637 만큼의 차이를 가진다.
        val y = cc[ChineseCalendar.EXTENDED_YEAR] - 2637
        val m = cc[ChineseCalendar.MONTH] + 1
        val d = cc[ChineseCalendar.DAY_OF_MONTH]

        val ret = StringBuffer()
        if (y < 1000) ret.append("0") else if (y < 100) ret.append("00") else if (y < 10) ret.append("000")
        ret.append(y)
        if (m < 10) ret.append("0")
        ret.append(m)
        if (d < 10) ret.append("0")
        ret.append(d)
        return ret.toString()
    }

    fun holidayArray(yyyy: String): List<Holiday> {
        val holidayList = holidaysMap[yyyy]
        if (holidayList != null) {
//            Logger.d("캐시")
            return holidayList
        }

//        Logger.d("캐시 안됨")

//        holidaysArrayList.clear() // 데이터 초기화
        // 양력 휴일
        addHolidaysItem(yyyy, "0101", "새해 첫날")
        addHolidaysItem(yyyy, "0301", "삼일절")
        addHolidaysItem(yyyy, "0505", "어린이날")
        addHolidaysItem(yyyy, "0606", "현충일")
        addHolidaysItem(yyyy, "0815", "광복절")
        addHolidaysItem(yyyy, "1003", "개천절")
        addHolidaysItem(yyyy, "1009", "한글날")
        addHolidaysItem(yyyy, "1225", "크리스마스")

        // 음력 휴일
//        val prev_seol = (lunarToSolar(yyyy + "0101").toInt() - 1).toString()
        var prev_seol_date = Date().stringToDate_yyyyMMdd(lunarToSolar(yyyy + "0101"))
        prev_seol_date = prev_seol_date.getNextDay(-1)
        val prev_seol = prev_seol_date.toString_yyyyMMdd()

        addHolidaysItem(yyyy, prev_seol.substring(4), "설날 연휴")
        addHolidaysItem(yyyy, solarDays(yyyy, "0101"), "설날")
        addHolidaysItem(yyyy, solarDays(yyyy, "0102"), "설날 연휴")
        addHolidaysItem(yyyy, solarDays(yyyy, "0408"), "부처님 오신 날")
        addHolidaysItem(yyyy, solarDays(yyyy, "0814"), "추석 연휴")
        addHolidaysItem(yyyy, solarDays(yyyy, "0815"), "추석")
        addHolidaysItem(yyyy, solarDays(yyyy, "0816"), "추석 연휴")

        try {
            // 어린이날 대체공휴일 검사 : 어린이날은 토요일, 일요일인 경우 그 다음 평일을 대체공유일로 지정
            val childDayChk = weekendValue(yyyy + "0505")
            if (childDayChk == 1)
                addHolidaysItem(yyyy, "0506", "대체공휴일")

            if (childDayChk == 7)
                addHolidaysItem(yyyy, "0507", "대체공휴일")

            // 설날 대체공휴일 검사
            if (weekendValue(lunarToSolar(yyyy + "0101")) == 1)
                addHolidaysItem(yyyy, solarDays(yyyy, "0103"), "대체공휴일")

            if (weekendValue(lunarToSolar(yyyy + "0101")) == 2)
                addHolidaysItem(yyyy, solarDays(yyyy, "0103"), "대체공휴일")

            if (weekendValue(lunarToSolar(yyyy + "0102")) == 1)
                addHolidaysItem(yyyy, solarDays(yyyy, "0103"), "대체공휴일")

            // 추석 대체공휴일 검사
            if (weekendValue(lunarToSolar(yyyy + "0814")) == 1)
                addHolidaysItem(yyyy, solarDays(yyyy, "0817"), "대체공휴일")

            if (weekendValue(lunarToSolar(yyyy + "0815")) == 1)
                addHolidaysItem(yyyy, solarDays(yyyy, "0817"), "대체공휴일")

            if (weekendValue(lunarToSolar(yyyy + "0816")) == 1)
                addHolidaysItem(yyyy, solarDays(yyyy, "0817"), "대체공휴일")

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        holidaysArrayList.sort() // 오름차순 정렬

        if (holidayList == null) {
            holidaysMap[yyyy] = holidaysArrayList
        }

        holidaysArrayList = Collections.synchronizedList(ArrayList<Holiday>())

        return holidaysMap[yyyy]!!
    }

    private fun solarDays(yyyy: String, date: String): String {
        return lunarToSolar(yyyy + date).substring(4)
    }

    private fun addHolidaysItem(year: String, date: String, name: String) {
        val item = Holiday(year, date, name)
        holidaysArrayList.add(item)
    }

    @Throws(ParseException::class)
    private fun weekendValue(date: String): Int {
        val sdf = SimpleDateFormat("yyyyMMdd")
        val cal = Calendar.getInstance()
        cal.time = sdf.parse(date)
        return cal[Calendar.DAY_OF_WEEK]
        // Calendar.SUNDAY : 1
        // Calendar.SATURDAY : 7
    }
}