package com.asusoft.calendar.activity

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import com.asusoft.calendar.*
import com.asusoft.calendar.dialog.DialogFragmentSelectYearMonth
import com.asusoft.calendar.fragment.day.FragmentDayCalendar
import com.asusoft.calendar.fragment.month.FragmentMonthViewPager
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.`object`.PreferenceKey
import com.asusoft.calendar.util.`object`.PreferenceManager
import com.google.android.material.appbar.AppBarLayout
import com.orhanobut.logger.Logger
import java.util.*


class ActivityStart : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    companion object {
        const val MONTH = 0
        const val Day = 1
    }

    private var date = Date().getToday()
    private var curFragmentIdx = PreferenceManager.getInt(PreferenceKey.SELECTED_CALENDAR_TYPE)
    private lateinit var changeFragmentButton: ImageButton
    private lateinit var fragmentLayout: RelativeLayout

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

//        Logger.d("toolbar height: ${toolbar.height}")
        
        toolbar.setOnClickListener {
            DialogFragmentSelectYearMonth.newInstance(date)
                    .show(supportFragmentManager, DialogFragmentSelectYearMonth.toString())
        }

        fragmentLayout = findViewById(R.id.fragment)

        changeFragmentButton = findViewById<ImageButton>(R.id.change_fragment_button)
        changeFragmentButton.setOnClickListener {
            curFragmentIdx++

            if (Day < curFragmentIdx) {
                curFragmentIdx = 0
            }

            changeRootFragment()
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportFragmentManager.addOnBackStackChangedListener(this)

        // remove shadow
        val appBarLayout = findViewById<AppBarLayout>(R.id.app_bar)
        val stateListAnimator = StateListAnimator()
        stateListAnimator.addState(IntArray(0), ObjectAnimator.ofFloat(appBarLayout, "elevation", 0f))
        appBarLayout.stateListAnimator = stateListAnimator

        if (savedInstanceState == null) {
            changeRootFragment()
        } else {
            onBackStackChanged()
        }
    }

    override fun onBackStackChanged() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount > 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun setDate(date: Date) {
        this.date = date
    }

    fun setTitle(text: String) {
        val tv = findViewById<TextView>(R.id.action_bar_title)
        tv.text = text
    }

    private fun clearFragmentStack() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        fragmentLayout.removeAllViews()
    }

    private fun changeRootFragment() {
        clearFragmentStack()

        when(curFragmentIdx) {
            MONTH -> {
//                Logger.d("change fragment date: ${date.toStringDay()}")
                supportFragmentManager.beginTransaction()
                        .add(R.id.fragment, FragmentMonthViewPager.newInstance(date), FragmentMonthViewPager.toString()).commit()
                changeFragmentButton.setImageResource(R.drawable.ic_baseline_format_list_bulleted_24)
            }

            Day -> {
//                Logger.d("change fragment date: ${date.toStringDay()}")
                supportFragmentManager.beginTransaction()
                        .add(R.id.fragment, FragmentDayCalendar.newInstance(date), FragmentDayCalendar.toString()).commit()
                changeFragmentButton.setImageResource(R.drawable.ic_baseline_calendar_today_24)
            }
        }

        PreferenceManager.setInt(PreferenceKey.SELECTED_CALENDAR_TYPE, curFragmentIdx)
    }
}