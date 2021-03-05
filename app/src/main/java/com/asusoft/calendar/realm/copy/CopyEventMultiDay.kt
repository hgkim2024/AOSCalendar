package com.asusoft.calendar.realm.copy

import com.asusoft.calendar.realm.RealmEventMultiDay
import com.asusoft.calendar.realm.RealmEventOneDay
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

class CopyEventMultiDay(
        val key: Long,
        var name: String,
        var startTime: Long,
        var endTime: Long,
        var isComplete: Boolean = false
) {

    fun updateIsCompete(isComplete: Boolean) {
        this.isComplete = isComplete
        val item = RealmEventMultiDay.select(key)
        item?.update(
                name,
                startTime,
                endTime,
                isComplete
        )
    }

    fun updateName(name: String) {
        this.name = name
        val item = RealmEventMultiDay.select(key)
        item?.update(
                name,
                startTime,
                endTime,
                isComplete
        )
    }

    fun delete() {
        val item = RealmEventOneDay.select(key)
        item?.delete()
    }
}