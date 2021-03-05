package com.asusoft.calendar.realm.copy

import com.asusoft.calendar.realm.RealmEventOneDay

class CopyEventOneDay(
        val key: Long,
        var name: String,
        var time: Long = 0,
        var isComplete: Boolean = false
) {

    fun updateIsCompete(isComplete: Boolean) {
        this.isComplete = isComplete
        val item = RealmEventOneDay.select(key)
        item?.update(
                name,
                time,
                isComplete
        )
    }

    fun updateName(name: String) {
        this.name = name
        val item = RealmEventOneDay.select(key)
        item?.update(
                name,
                time,
                isComplete
        )
    }

    fun delete() {
        val item = RealmEventOneDay.select(key)
        item?.delete()
    }
}
