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
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import io.realm.Realm
import io.realm.RealmConfiguration


class CalendarApplication: Application() {

    companion object {
        lateinit var context: Context

        fun getRealmConfig(): RealmConfiguration {
//            return RealmConfiguration.Builder()
//                    .deleteRealmIfMigrationNeeded()
//                    .build()

            return RealmConfiguration.Builder()
                    .schemaVersion(1)
                    .migration { realm, oldVersion, newVersion ->

                        val schema = realm.schema

                        if (oldVersion == 0L) {
                            val realmEventMultiDay = schema.get("RealmEventMultiDay")
                            realmEventMultiDay?.addField("isComplete", Boolean.javaClass, null)

                            val realmEventOneDay = schema.get("RealmEventOneDay")
                            realmEventOneDay?.addField("isComplete", Boolean.javaClass, null)
                        }
                    }
                    .build()
        }

        fun getColor(id: Int): Int {
            return ContextCompat.getColor(context, id)
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Realm 초기화
        Realm.init(this)

        // Util 초기화
        CalculatorUtil.setContext(baseContext)
        AdUtil.setContext(baseContext)
        context = baseContext

        // AdMob 광고 초기화
        MobileAds.initialize(this) {}

        if (BuildConfig.DEBUG) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                    .build()
            )
        }

        // 로그 초기화
        Logger.addLogAdapter(object : AndroidLogAdapter() {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
    }

}