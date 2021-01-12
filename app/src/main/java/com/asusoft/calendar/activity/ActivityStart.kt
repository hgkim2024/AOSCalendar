package com.asusoft.calendar.activity

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.asusoft.calendar.*
import com.asusoft.calendar.fragment.month.FragmentMonthViewPager
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.getToday
import com.asusoft.calendar.util.prevMonth
import com.asusoft.calendar.util.startOfMonth
import java.util.*
import kotlin.collections.ArrayList


class ActivityStart : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: - 실제 기기에서 적용되는지 테스트해보기 - 안드로이드 10 기기가 없음
//        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
//            setTheme(R.style.DarkTheme);
//        } else {
//            setTheme(R.style.LightTheme);
//        }

        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false);
        supportFragmentManager.addOnBackStackChangedListener(this)

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction().add(R.id.fragment, FragmentMonthViewPager.newInstance(), "FragmentMonthViewPager").commit()
        else
            onBackStackChanged()
    }

    override fun onBackStackChanged() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount > 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun setTitle(text: String) {
        val tv = findViewById<TextView>(R.id.action_bar_title)
        tv.text = text
    }
}