package com.asusoft.calendar.realm.copy

import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.util.startOfDay

class CopyEventDay(
        val key: Long,
        var name: String,
        var startTime: Long,
        var endTime: Long,
        var isComplete: Boolean = false,
        var visitList: ArrayList<CopyVisitPerson>,
        var memo: String,
        var color: Int,
        var order: Long
) {

    fun insert() {
        val item = RealmEventDay()
        item.updateKey(key)
        item.update(
            name,
            startTime,
            endTime,
            isComplete,
            visitList,
            memo,
            color,
            order
        )
        item.insert()
    }

    fun updateIsCompete(isComplete: Boolean) {
        this.isComplete = isComplete
        val item = RealmEventDay.selectOne(key)
        item?.update(
                name,
                startTime,
                endTime,
                isComplete
        )
    }

    fun updateName(name: String) {
        this.name = name
        val item = RealmEventDay.selectOne(key)
        item?.update(
                name,
                startTime,
                endTime,
                isComplete
        )
    }

    fun updateOrder(order: Long) {
        this.order = order
        val item = RealmEventDay.selectOne(key)
        item?.updateOrder(order)
    }

    fun delete() {
        val item = RealmEventDay.selectOne(key)
        item?.delete()
    }
}