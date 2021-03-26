package com.asusoft.calendar.activity.setting.activity

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.addEvent.activity.ActivityAddPerson
import com.asusoft.calendar.activity.setting.fragment.FragmentSetting
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.google.android.material.appbar.AppBarLayout
import java.util.HashMap

class ActivitySetting : AppCompatActivity() {

    companion object {
        override fun toString(): String {
            return "ActivitySetting"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

        if (savedInstanceState == null)
            supportFragmentManager
                    .beginTransaction()
                    .add(
                            R.id.fragment,
                            FragmentSetting.newInstance(),
                            FragmentSetting.toString()
                    )
                    .commit()
    }

    override fun finish() {
        val event = HashMapEvent(HashMap())
        event.map[ActivitySetting.toString()] = ActivitySetting.toString()
        GlobalBus.post(event)
        super.finish()
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            android.R.id.home -> {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

}