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

open class RealmEventOneDay: RealmObject() {

    @PrimaryKey
    var key: Long = System.currentTimeMillis()

    var name: String = ""

    @Index
    var time: Long = 0

    companion object {
        // TODO: - 쿼리 테스트 해볼 것

        fun select(date: Date): List<RealmEventOneDay> {
            val startTime = date.startOfWeek.time
            val endTime = date.endOfWeek.time

            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()

            val item = realm.where(RealmEventOneDay::class.java)
                    .greaterThanOrEqualTo("time", startTime)
                    .and()
                    .lessThanOrEqualTo("time", endTime)
                    .findAll()

            realm.commitTransaction()

            Log.d("Asu", "RealmEventOneDay date: ${date.startOfWeek.toStringDay()}, List: $item")
            return item
        }
    }


    fun update(
        name: String,
        time: Long
    ) {
        val realm = Realm.getInstance(CalendarApplication.getRealmConfig())

        realm.beginTransaction()

        if (name != "") {
            this.name = name
        }

        if (time > 0) {
            this.time = time
        }

        Log.d("Asu", "RealmEventOneDay update, name: ${name}, time: ${Date(time).toStringDay()}")
        realm.commitTransaction()
    }
}