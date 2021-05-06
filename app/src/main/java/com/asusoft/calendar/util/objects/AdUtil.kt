package com.asusoft.calendar.util.objects

import com.asusoft.calendar.BuildConfig
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication

object AdUtil {
    private lateinit var context: CalendarApplication

    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

    val adMobAppId: String
        get() {
            return context.getString(R.string.admod_app_id)
        }

    val adMobAddEventTopBannerId: String
        get() {
            return if (BuildConfig.DEBUG) {
                TEST_BANNER_ID
            } else {
                "ca-app-pub-1887982661173003/9669586205"
            }
        }

    fun setContext(context: CalendarApplication) {
        this.context = context
    }
}