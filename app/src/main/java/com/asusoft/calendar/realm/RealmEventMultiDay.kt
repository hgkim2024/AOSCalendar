package com.asusoft.calendar.realm

import android.util.Log
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.util.endOfWeek
import com.asusoft.calendar.util.startOfWeek
import com.asusoft.calendar.util.toStringDay
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import java.util.*

open class RealmEventMultiDay: RealmObject() {

    @PrimaryKey
    var key: Long = System.currentTimeMillis()

    var name: String = ""

    @Index
    var startTime: Long = 0

    @Index
    var endTime: Long = 0

    companion object {
        fun select(date: Date): List<RealmEventMultiDay> {
            val startTime = date.startOfWeek.time
            val endTime = date.endOfWeek.time

            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()

            val item = realm.where(RealmEventMultiDay::class.java)

                    .greaterThanOrEqualTo("startTime", startTime)
                    .and()
                    .lessThanOrEqualTo("startTime", endTime)

                    .or()

                    .greaterThanOrEqualTo("endTime", startTime)
                    .and()
                    .lessThanOrEqualTo("endTime", endTime)
                    
                    .findAll()
            
            realm.commitTransaction()

//            Log.d("Asu", "RealmEventMultiDay date: ${date.startOfWeek.toStringDay()}, List: $item")
            return item
        }
    }


    fun update(
        name: String,
        startTime: Long,
        endTime: Long
    ) {
        val realm = Realm.getInstance(CalendarApplication.getRealmConfig())

        realm.beginTransaction()

        if (name != "") {
            this.name = name
        }

        if (startTime > 0) {
            this.startTime = startTime
        }

        if (endTime > 0) {
            this.endTime = endTime
        }

//        Log.d("Asu", "RealmEventOneDay update, name: ${name}, startTime: ${Date(startTime).toStringDay()}, endTime: ${Date(endTime).toStringDay()}")
        realm.commitTransaction()
    }

    fun insert() {
        val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
        realm.beginTransaction()
        realm.insertOrUpdate(this)
        realm.commitTransaction()
    }
}