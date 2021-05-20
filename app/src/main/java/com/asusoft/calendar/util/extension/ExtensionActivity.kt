package com.asusoft.calendar.util.extension

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.app.Activity
import com.asusoft.calendar.R
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