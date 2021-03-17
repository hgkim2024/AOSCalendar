package com.asusoft.calendar.realm

import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.copy.CopyVisitPerson
import com.orhanobut.logger.Logger
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmVisitPerson: RealmObject() {
    @PrimaryKey
    var key: Long = System.currentTimeMillis()

    var name: String = ""

    var phone: String = ""

    fun getCopy(): CopyVisitPerson {
        return CopyVisitPerson(
                name,
                phone
        )
    }

    fun update(
            name: String,
            phone: String,
    ) {
        val realm = Realm.getInstance(CalendarApplication.getRealmConfig())

        realm.beginTransaction()

        if (name != "") {
            this.name = name
        }

        if (phone != "") {
            this.phone = phone
        }

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