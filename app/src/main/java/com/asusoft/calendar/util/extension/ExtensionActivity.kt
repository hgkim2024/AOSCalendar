package com.asusoft.calendar.util.extension

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.asusoft.calendar.R
import com.asusoft.calendar.util.objects.PreferenceKey
import com.asusoft.calendar.util.objects.PreferenceManager
import com.google.android.material.appbar.AppBarLayout


fun Activity.removeActionBarShadow() {
    // remove shadow
    val appBarLayout = findViewById<AppBarLayout>(R.id.app_bar)
    val stateListAnimator = StateListAnimator()
    stateListAnimator.addState(
            IntArray(0),
            ObjectAnimator.ofFloat(
                    appBarLayout,
                    "elevation",
                    0f
            )
    )
    appBarLayout.stateListAnimator = stateListAnimator
}

fun Activity.setOrientation() {
    val orientation = PreferenceManager.getInt(PreferenceKey.CALENDAR_ORIENTATION, PreferenceKey.CALENDAR_DEFAULT_ORIENTATION)

    when(orientation) {
        0 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        1 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }
}