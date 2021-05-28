package com.asusoft.calendar.realm.copy

data class CopyTheme(
    var key: Long,
    var colorAccent: Int,
    var holiday: Int,
    var saturday: Int,
    var background: Int,
    var separator: Int,
    var font: Int,
    var lightFont: Int,
    var invertFont: Int,
    var today: Int,
    var eventFontColor: Int
)
