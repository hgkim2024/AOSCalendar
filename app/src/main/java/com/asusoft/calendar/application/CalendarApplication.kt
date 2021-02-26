package com.asusoft.calendar.application

import android.app.Application
import android.content.Context
import androidx.core.content.ContextCompat
import com.asusoft.calendar.BuildConfig
import com.asusoft.calendar.util.`object`.AdUtil
import com.asusoft.calendar.util.`object`.CalculatorUtil
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import io.realm.Realm
import io.realm.RealmConfiguration

class CalendarApplication: Application() {

    companion object {
        lateinit var context: Context

        fun getRealmConfig(): RealmConfiguration {
            return RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build()
        }

        fun getColor(id: Int): Int {
            return ContextCompat.getColor(context, id)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        CalculatorUtil.setContext(baseContext)
        AdUtil.setContext(baseContext)
        context = baseContext

        MobileAds.initialize(this) {}

        if (BuildConfig.DEBUG) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                    .build()
            )
        }
    }

}