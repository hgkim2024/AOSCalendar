package com.asusoft.calendar.realm

import android.util.Log
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.copy.CopyEventOneDay
import com.asusoft.calendar.util.*
import com.orhanobut.logger.Logger
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

open class RealmEventOneDay: RealmObject() {

    @PrimaryKey
    var key: Long = System.currentTimeMillis()

    var name: String = ""

    @Index
    var time: Long = 0

    var isComplete: Boolean = false

    companion object {
        fun selectOneWeek(date: Date): List<RealmEventOneDay> {
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

//            Logger.d("RealmEventOneDay date: ${Date(startTime).toStringDay()}, List: $item")
            return item
        }

        private fun selectOneDay(date: Date): List<RealmEventOneDay> {
            val startTime = date.startOfDay.time
            val endTime = date.endOfDay.time

            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()

            val item = realm.where(RealmEventOneDay::class.java)
                    .greaterThanOrEqualTo("time", startTime)
                    .and()
                    .lessThanOrEqualTo("time", endTime)
                    .findAll()

            realm.commitTransaction()

//            Logger.d("RealmEventOneDay date: ${Date(startTime).toStringDay()}, List: $item")
            return item
        }

        fun getOneDayCopyList(date: Date): ArrayList<CopyEventOneDay> {
            val realmList = selectOneDay(date)
            val copyList = ArrayList<CopyEventOneDay>()

            for (item in realmList) {
                copyList.add(
                        CopyEventOneDay(
                                item.key,
                                item.name,
                                item.time
                        )
                )
            }

            return copyList
        }

        fun select(key: Long): RealmEventOneDay? {
            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()

            val item = realm.where(RealmEventOneDay::class.java).equalTo("key", key).findFirst()

            realm.commitTransaction()

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

//        Logger.d("RealmEventOneDay update, name: ${name}, time: ${Date(time).toStringDay()}")
        realm.commitTransaction()
    }

    fun insert() {
        val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
        realm.beginTransaction()
        realm.insertOrUpdate(this)
        realm.commitTransaction()
    }

    fun delete() {
        val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
        realm.beginTransaction()
        this.deleteFromRealm()
        realm.commitTransaction()
    }
}