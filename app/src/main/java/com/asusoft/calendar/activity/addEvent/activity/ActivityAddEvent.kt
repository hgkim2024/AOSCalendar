package com.asusoft.calendar.activity.addEvent.activity

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentManager
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.addEvent.fragment.FragmentAddEvent
import com.asusoft.calendar.util.extension.removeActionBarShadow
import com.asusoft.calendar.util.extension.setOrientation
import com.asusoft.calendar.util.objects.CalendarUtil
import com.asusoft.calendar.util.objects.PreferenceKey
import com.asusoft.calendar.util.objects.PreferenceManager
import com.asusoft.calendar.util.objects.ThemeUtil


class ActivityAddEvent: AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    var key = -1L
    var refreshFlag = false

    private var startDate: Long = 0
    private var endDate: Long = 0

    companion object {
        fun toStringActivity(): String {
            return "ActivityAddEvent"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        setOrientation()

        val rootLayout = findViewById<CoordinatorLayout>(R.id.root_layout)
        rootLayout.setBackgroundColor(ThemeUtil.instance.background)

        val title = findViewById<TextView>(R.id.action_bar_title)
        title.setTextColor(ThemeUtil.instance.font)

        val fragment = findViewById<RelativeLayout>(R.id.fragment)
        fragment.setBackgroundColor(ThemeUtil.instance.background)

        key = intent.getLongExtra("key", -1L)

        if (key == -1L) {
            startDate = intent.getSerializableExtra("startDate") as Long
            endDate = intent.getSerializableExtra("endDate") as Long
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setBackgroundColor(ThemeUtil.instance.background)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportFragmentManager.addOnBackStackChangedListener(this)

        removeActionBarShadow()

        if (savedInstanceState == null)
            supportFragmentManager
                .beginTransaction()
                .add(
                        R.id.fragment,
                        FragmentAddEvent.newInstance(key, startDate, endDate),
                        FragmentAddEvent.toString()
                )
                .commit()
        else
            onBackStackChanged()
    }

    override fun onBackStackChanged() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount > 0)
    }

    override fun onResume() {
        super.onResume()
        setOrientation()
    }

    override fun finish() {

        val focusView: View? = currentFocus
        if (focusView != null) {
            val imm: InputMethodManager? = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(focusView.windowToken, 0)
            focusView.clearFocus()
        }

        if (refreshFlag) {
            CalendarUtil.calendarRefresh(true)
        }

        super.finish()
    }

    fun setTitle(text: String) {
        val tv = findViewById<TextView>(R.id.action_bar_title)
        PreferenceManager.getFloat(PreferenceKey.CALENDAR_HEADER_FONT_SIZE, PreferenceKey.CALENDAR_HEADER_DEFAULT_FONT_SIZE)
        tv.text = text
    }
}