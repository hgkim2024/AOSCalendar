package com.asusoft.calendar.realm

import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.util.endOfWeek
import com.asusoft.calendar.util.startOfWeek
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import java.util.*

open class EventMultiDay: RealmObject() {

    @PrimaryKey
    var key: Long = System.currentTimeMillis()

    var name: String = ""

    @Index
    var startTime: Long = 0

    @Index
    var endTime: Long = 0

    companion object {
        // TODO: - 쿼리 테스트 해볼 것

        fun select(date: Date): List<EventMultiDay> {
            val startTime = date.startOfWeek.time
            val endTime = date.endOfWeek.time

            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()
            val item = realm.where(EventMultiDay::class.java)

                    .greaterThanOrEqualTo("startTime", startTime)
                    .and()
                    .lessThanOrEqualTo("startTime", endTime)

                    .or()

                    .greaterThanOrEqualTo("endTime", startTime)
                    .and()
                    .lessThanOrEqualTo("endTime", endTime)
                    
                    .findAll()
            
            realm.commitTransaction()
            return item
        }
    }

}