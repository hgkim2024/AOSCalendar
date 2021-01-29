package com.asusoft.calendar.application

import android.app.Application
import com.asusoft.calendar.util.`object`.CalculatorUtil
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
        CalculatorUtil.setContext(baseContext)
    }

}