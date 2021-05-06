package com.asusoft.calendar.realm

import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.dialog.filter.enums.DateFilterType
import com.asusoft.calendar.activity.calendar.dialog.filter.enums.StringFilterType
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.copy.CopyEventDay
import com.asusoft.calendar.realm.copy.CopyVisitPerson
import com.asusoft.calendar.util.*
import com.orhanobut.logger.Logger
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

open class RealmEventDay: RealmObject() {

    @PrimaryKey
    var key: Long = System.currentTimeMillis()

    var name: String = ""

    @Index
    var startTime: Long = 0

    @Index
    var endTime: Long = 0

    var isComplete: Boolean = false

    var visitList: RealmList<RealmVisitPerson> = RealmList()

    var memo: String = ""

    var color: Int = 0

    companion object {
        fun selectOneWeek(date: Date): List<RealmEventDay> {
            val startTime = date.startOfWeek.time
            val endTime = date.endOfWeek.time

            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()

            val item = realm.where(RealmEventDay::class.java)

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

        private fun selectOneDay(date: Date): List<RealmEventDay> {
            val startTime = date.startOfDay.time
            val endTime = date.endOfDay.time

            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()

            val item = realm.where(RealmEventDay::class.java)

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

        fun getOneDayCopyList(
                date: Date,
                isVisitList: Boolean = false
        ): ArrayList<CopyEventDay> {

            val realmList = selectOneDay(date)
            val copyList = ArrayList<CopyEventDay>()

            for (item in realmList) {
                copyList.add(
                        item.getCopy(isVisitList)
                )
            }

            return copyList
        }

        fun select(key: Long): RealmEventDay? {
            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()

            val item = realm.where(RealmEventDay::class.java)
                    .equalTo("key", key)
                    .findFirst()

            realm.commitTransaction()

            return item
        }

//        fun selectCopyList(name: String): ArrayList<CopyEventDay> {
//            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
//            realm.beginTransaction()
//
//            val items = realm.where(RealmEventDay::class.java)
//                    .like("name", "*${name}*")
//                    .findAll()
//
//            realm.commitTransaction()
//
//            val list = ArrayList<CopyEventDay>()
//
//            for (item in items) {
//                list.add(item.getCopy(true))
//            }
//
//            return list
//        }

        fun selectCopyList(name: String, searchType: StringFilterType, periodType: DateFilterType): ArrayList<CopyEventDay> {
            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()

            if (searchType != StringFilterType.VISIT_NAME) {
                val today = Date().getToday()
                val time = when(periodType) {
                    DateFilterType.ALL -> 0L
                    DateFilterType.MONTH_1 -> today.prevMonth.time
                    DateFilterType.MONTH_3 -> today.getNextMonth(-3).time
                    DateFilterType.MONTH_6 -> today.getNextMonth(-6).time
                }

                val q = realm.where(RealmEventDay::class.java).greaterThanOrEqualTo("endTime", time)
                val query = when (searchType) {
                    StringFilterType.EVENT_NAME -> q.like("name", "*${name}*")
                    StringFilterType.VISIT_NAME -> null
                    StringFilterType.MEMO -> q.like("memo", "*${name}*")
                }

                val items = query!!.findAll()
                realm.commitTransaction()

                val list = ArrayList<CopyEventDay>()

                for (item in items) {
                    list.add(item.getCopy(true))
                }

                return list
            } else {
                val today = Date().getToday()
                val time = when(periodType) {
                    DateFilterType.ALL -> 0L
                    DateFilterType.MONTH_1 -> today.prevMonth.time
                    DateFilterType.MONTH_3 -> today.getNextMonth(-3).time
                    DateFilterType.MONTH_6 -> today.getNextMonth(-6).time
                }

                val items = realm.where(RealmEventDay::class.java).greaterThanOrEqualTo("endTime", time).findAll()
                val visitItems = realm.where(RealmVisitPerson::class.java).like("name", "*${name}*").findAll()
                realm.commitTransaction()

                val list = ArrayList<CopyEventDay>()
                val filterList = ArrayList<CopyEventDay>()

                for (item in items) {
                    list.add(item.getCopy(true))
                }

                for (item in visitItems) {
                    filterList.addAll(list.filter {it.key == item.foreignKey})
                }

                return ArrayList(filterList.distinct())
            }

        }
    }

    fun getCopy(isVisitList: Boolean = false): CopyEventDay {
        val visitList = ArrayList<CopyVisitPerson>()
        if (isVisitList) {
            visitList.addAll(getCopyVisitList())
        }

        return CopyEventDay(
                key,
                name,
                startTime,
                endTime,
                isComplete,
                visitList,
                memo,
                color
        )
    }

    fun getCopyVisitList(): ArrayList<CopyVisitPerson> {
        val copyVisitList = ArrayList<CopyVisitPerson>()

        for (visitPerson in visitList) {
            copyVisitList.add(visitPerson.getCopy())
        }

        return copyVisitList
    }

    fun update(
        name: String,
        startTime: Long,
        endTime: Long,
        isComplete: Boolean,
        visitList: ArrayList<CopyVisitPerson>? = null,
        memo: String? = null,
        color: Int? = null
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
        
        if (visitList != null) {
            while(this.visitList.isNotEmpty()) {
                val item = this.visitList.removeAt(0)
                item.deleteFromRealm()
            }

            for (visitPerson in visitList) {
                val item = RealmVisitPerson()
                item.foreignKey = key
                item.name = visitPerson.name
                item.phone = visitPerson.phone
                this.visitList.add(item)
            }
        }

        if (memo != null) {
            this.memo = memo
        }

        this.isComplete = isComplete
//        Logger.d("update isComplete: ${this.isComplete}")

        if (color != null) {
            this.color = color
        }

//        Logger.d("RealmEventOneDay update, name: ${name}, startTime: ${Date(startTime).toStringDay()}, endTime: ${Date(endTime).toStringDay()}")
        realm.commitTransaction()
        realm.refresh()
    }

    fun insert() {
        val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
        realm.beginTransaction()
        realm.insertOrUpdate(this)
        realm.commitTransaction()
        realm.refresh()
    }

    fun delete() {
        val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
        realm.beginTransaction()
        this.deleteFromRealm()
        realm.commitTransaction()
        realm.refresh()
    }
}