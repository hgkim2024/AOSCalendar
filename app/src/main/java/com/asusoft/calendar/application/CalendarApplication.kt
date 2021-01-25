package com.asusoft.calendar.application

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class CalendarApplication: Application() {

    companion object {
        fun getRealmConfig(): RealmConfiguration {
            return RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }

}