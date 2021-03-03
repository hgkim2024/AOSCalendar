package com.asusoft.calendar.realm

import android.util.Log
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.copy.CopyEventMultiDay
import com.asusoft.calendar.realm.copy.CopyEventOneDay
import com.asusoft.calendar.util.*
import com.orhanobut.logger.Logger
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

    var isComplete: Boolean = false

    companion object {
        fun selectOneWeek(date: Date): List<RealmEventMultiDay> {
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

                    .or()

                    .lessThanOrEqualTo("startTime", startTime)
                    .and()
                    .greaterThanOrEqualTo("endTime", endTime)
                    
                    .findAll()
            
            realm.commitTransaction()

//            Logger.d("RealmEventMultiDay date: ${date.startOfWeek.toStringDay()}, List: $item")
            return item
        }

        private fun selectOneDay(date: Date): List<RealmEventMultiDay> {
            val startTime = date.startOfDay.time
            val endTime = date.endOfDay.time

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

                    .or()

                    .lessThanOrEqualTo("startTime", startTime)
                    .and()
                    .greaterThanOrEqualTo("endTime", endTime)

                    .findAll()

            realm.commitTransaction()

//            Logger.d("RealmEventMultiDay date: ${date.startOfWeek.toStringDay()}, List: $item")
            return item
        }

        fun getOneDayCopyList(date: Date): ArrayList<CopyEventMultiDay> {
            val realmList = selectOneDay(date)
            val copyList = ArrayList<CopyEventMultiDay>()

            for (item in realmList) {
                copyList.add(
                        CopyEventMultiDay(
                                item.key,
                                item.name,
                                item.startTime,
                                item.endTime
                        )
                )
            }

            return copyList
        }

        fun select(key: Long): RealmEventMultiDay? {
            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()

            val item = realm.where(RealmEventMultiDay::class.java).equalTo("key", key).findFirst()

            realm.commitTransaction()

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

//        Logger.d("RealmEventOneDay update, name: ${name}, startTime: ${Date(startTime).toStringDay()}, endTime: ${Date(endTime).toStringDay()}")
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