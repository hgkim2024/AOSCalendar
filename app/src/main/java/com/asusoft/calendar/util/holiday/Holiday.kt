package com.asusoft.calendar.util.holiday

class Holiday : Comparable<Holiday> {

    var year: String
    var date: String
    var name: String

    constructor(year: String, date: String, name: String) {
        this.year = year
        this.date = date
        this.name = name
    }

    override fun compareTo(holiday: Holiday): Int {
        return date.compareTo(holiday.date)
    }
}