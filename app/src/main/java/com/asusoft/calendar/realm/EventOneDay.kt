package com.asusoft.calendar.realm

import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.util.endOfWeek
import com.asusoft.calendar.util.startOfWeek
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import java.util.*

open class EventOneDay: RealmObject() {

    @PrimaryKey
    var key: Long = System.currentTimeMillis()

    var name: String = ""

    @Index
    var time: Long = 0

    companion object {
        fun select(date: Date): List<EventOneDay> {
            val startTime = date.startOfWeek.time
            val endTime = date.endOfWeek.time

            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()
            val item = realm.where(EventOneDay::class.java)
                    .greaterThanOrEqualTo("startTime", startTime)
                    .lessThanOrEqualTo("endTime", endTime)
                    .findAll()
            realm.commitTransaction()
            return item
        }
    }
}