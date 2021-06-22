package com.asusoft.calendar.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Size
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.asusoft.calendar.BuildConfig
import com.asusoft.calendar.util.objects.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import io.realm.Realm
import io.realm.RealmConfiguration


class CalendarApplication: Application() {

    companion object {
        lateinit var instance: CalendarApplication
        const val THROTTLE = 1000L

        fun getContext(): Context {
            return instance.baseContext
        }

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
            return ContextCompat.getColor(getContext(), id)
        }

        fun getColorList(id: Int): ColorStateList? {
            return ContextCompat.getColorStateList(getContext(), id)
        }

        fun getDrawable(id: Int): Drawable? {
            return ContextCompat.getDrawable(getContext(), id)
        }

        fun getSize(activity: Activity): Size {
            val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val width: Int
            val height: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = wm.currentWindowMetrics
                val windowInsets: WindowInsets = windowMetrics.windowInsets

                val insets = windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
                val insetsWidth = insets.right + insets.left
                val insetsHeight = insets.top + insets.bottom

                val b = windowMetrics.bounds
                width = b.width() - insetsWidth
                height = b.height() - insetsHeight
            } else {
                val size = Point()
                val display = wm.defaultDisplay // deprecated in API 30
                display?.getSize(size) // deprecated in API 30
                width = size.x
                height = size.y
            }

            return Size(width, height)
        }
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        // Realm 초기화
        Realm.init(this)

        // Util 초기화
        CalculatorUtil.setContext(this)
        AdUtil.setContext(this)

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
        PreferenceManager.setApplicationContext(this)
    }
}