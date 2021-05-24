package com.asusoft.calendar.activity.setting.activity

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.addEvent.activity.ActivityAddPerson
import com.asusoft.calendar.activity.setting.fragment.FragmentSetting
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.removeActionBarShadow
import com.asusoft.calendar.util.extension.setOrientation
import com.asusoft.calendar.util.objects.ThemeUtil
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

        setOrientation()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        removeActionBarShadow()

        val title = findViewById<TextView>(R.id.action_bar_title)
        title.setTextColor(ThemeUtil.instance.font)

        val fragmentLayout = findViewById<RelativeLayout>(R.id.fragment)
        fragmentLayout.setBackgroundColor(ThemeUtil.instance.background)

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

    override fun onResume() {
        super.onResume()
        setOrientation()
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