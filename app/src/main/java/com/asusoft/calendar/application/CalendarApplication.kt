package com.asusoft.calendar.application

import android.app.Application
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.asusoft.calendar.BuildConfig
import com.asusoft.calendar.realm.MyRealmMigration
import com.asusoft.calendar.util.`object`.AdUtil
import com.asusoft.calendar.util.`object`.CalculatorUtil
import com.asusoft.calendar.util.`object`.PreferenceKey
import com.asusoft.calendar.util.`object`.PreferenceManager
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
        const val THROTTLE = 1000L

        fun getRealmConfig(): RealmConfiguration {
            return RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build()

//            return RealmConfiguration.Builder()
//                    .schemaVersion(1)
//                    .migration(MyRealmMigration())
//                    .build()
        }

        fun getColor(id: Int): Int {
            return ContextCompat.getColor(context, id)
        }

        fun getColorList(id: Int): ColorStateList? {
            return ContextCompat.getColorStateList(context, id)
        }

        fun getDrawable(id: Int): Drawable? {
            return ContextCompat.getDrawable(context, id)
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

        // 공유 레퍼런스 context 초기화
        PreferenceManager.setApplicationContext(baseContext)
        initPreference()
    }


    private fun initPreference() {

        val selectedCalendarType = PreferenceManager.getInt(PreferenceKey.SELECTED_CALENDAR_TYPE)
        if (selectedCalendarType == PreferenceManager.DEFAULT_VALUE_INT) {
            PreferenceManager.setInt(PreferenceKey.SELECTED_CALENDAR_TYPE, 0)
        }

    }
}