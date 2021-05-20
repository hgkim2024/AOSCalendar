package com.asusoft.calendar.util.recyclerview.holder.setting.seekbar

data class SeekBarItem (
        var key: String,
        var value: Int,
        var title: String,
        var min: Int,
        var max: Int,
        var defaultValue: Int
)