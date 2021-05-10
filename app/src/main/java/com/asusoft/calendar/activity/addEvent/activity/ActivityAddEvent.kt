package com.asusoft.calendar.activity.addEvent.activity

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.addEvent.fragment.FragmentAddEvent
import com.asusoft.calendar.util.objects.CalendarUtil
import com.google.android.material.appbar.AppBarLayout


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

        key = intent.getLongExtra("key", -1L)

        if (key == -1L) {
            startDate = intent.getSerializableExtra("startDate") as Long
            endDate = intent.getSerializableExtra("endDate") as Long
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportFragmentManager.addOnBackStackChangedListener(this)

        // remove shadow
        val appBarLayout = findViewById<AppBarLayout>(R.id.app_bar)
        val stateListAnimator = StateListAnimator()
        stateListAnimator.addState(
                IntArray(0), ObjectAnimator.ofFloat(
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
                    FragmentAddEvent.
                    newInstance(key, startDate, endDate),
                    FragmentAddEvent.toString()
                )
                .commit()
        else
            onBackStackChanged()
    }

    override fun onBackStackChanged() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount > 0)
    }

    override fun finish() {
        if (refreshFlag) {
            CalendarUtil.calendarRefresh(true)
        }

        super.finish()
    }

    fun setTitle(text: String) {
        val tv = findViewById<TextView>(R.id.action_bar_title)
        tv.text = text
    }
}