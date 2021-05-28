package com.asusoft.calendar.activity.setting.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.setting.fragment.FragmentSetting
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.removeActionBarShadow
import com.asusoft.calendar.util.extension.setOrientation
import com.asusoft.calendar.util.objects.AlertUtil
import com.asusoft.calendar.util.objects.EventBackupAndRestoreUtil
import com.asusoft.calendar.util.objects.PreferenceManager.context
import com.asusoft.calendar.util.objects.ThemeUtil
import com.asusoft.calendar.util.toString_yyyyMMdd_HHmmss
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

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