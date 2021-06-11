package com.asusoft.calendar.activity.setting.fragment.font

import com.asusoft.calendar.util.objects.PreferenceKey
import com.asusoft.calendar.util.objects.PreferenceManager
import com.asusoft.calendar.util.recyclerview.holder.setting.seekbar.SeekBarItem

enum class SettingFontSizeType(val value: Int) {

    CALENDAR_HEADER(0),

    MONTH_HEADER(1),
    MONTH_DAY(2),
    MONTH_ITEM(3),
    MONTH_COUNTER(4),

    WEEK_HEADER(5),
    WEEK_ITEM(6),

    DAY_HEADER(7),
    DAY_ITEM(8);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }

    fun getSeekBarItem(): SeekBarItem {

        var key = ""
        var default = 0
        var title = ""

        when (this) {
            CALENDAR_HEADER -> {
                key = PreferenceKey.CALENDAR_HEADER_FONT_SIZE
                default = PreferenceKey.CALENDAR_HEADER_DEFAULT_FONT_SIZE.toInt()
                title = "상단 바"
            }



            MONTH_HEADER -> {
                key = PreferenceKey.MONTH_CALENDAR_HEADER_FONT_SIZE
                default = PreferenceKey.MONTH_CALENDAR_HEADER_DEFAULT_FONT_SIZE.toInt()
                title = "요일"
            }

            MONTH_DAY -> {
                key = PreferenceKey.MONTH_CALENDAR_DAY_FONT_SIZE
                default = PreferenceKey.MONTH_CALENDAR_DAY_DEFAULT_FONT_SIZE.toInt()
                title = "날짜"
            }

            MONTH_ITEM -> {
                key = PreferenceKey.MONTH_CALENDAR_EVENT_FONT_SIZE
                default = PreferenceKey.MONTH_CALENDAR_EVENT_DEFAULT_FONT_SIZE.toInt()
                title = "이벤트"
            }

            MONTH_COUNTER -> {
                key = PreferenceKey.MONTH_CALENDAR_COUNTER_FONT_SIZE
                default = PreferenceKey.MONTH_CALENDAR_COUNTER_DEFAULT_FONT_SIZE.toInt()
                title = "표기못한 이벤트 숫자"
            }



            WEEK_HEADER -> {
                key = PreferenceKey.WEEK_CALENDAR_HEADER_FONT_SIZE
                default = PreferenceKey.WEEK_CALENDAR_HEADER_DEFAULT_FONT_SIZE.toInt()
                title = "요일"
            }

            WEEK_ITEM -> {
                key = PreferenceKey.WEEK_CALENDAR_EVENT_FONT_SIZE
                default = PreferenceKey.WEEK_CALENDAR_EVENT_DEFAULT_FONT_SIZE.toInt()
                title = "이벤트"
            }



            DAY_HEADER -> {
                key = PreferenceKey.DAY_CALENDAR_HEADER_FONT_SIZE
                default = PreferenceKey.DAY_CALENDAR_HEADER_DEFAULT_FONT_SIZE.toInt()
                title = "상단 바"
            }

            DAY_ITEM -> {
                key = PreferenceKey.DAY_CALENDAR_ITEM_FONT_SIZE
                default = PreferenceKey.DAY_CALENDAR_ITEM_DEFAULT_FONT_SIZE.toInt()
                title = "이벤트"
            }
       }

        return SeekBarItem(
                key,
                PreferenceManager.getFloat(key, default.toFloat()).toInt(),
                title,
                default - 8,
                default + 8,
                default
        )
    }
}