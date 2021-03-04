package com.asusoft.calendar.activity

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import com.asusoft.calendar.*
import com.asusoft.calendar.dialog.DialogFragmentSelectYearMonth
import com.asusoft.calendar.fragment.month.FragmentMonthViewPager
import com.asusoft.calendar.util.*
import com.google.android.material.appbar.AppBarLayout
import java.util.*


class ActivityStart : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {
    private var date = Date().getToday()

    @RequiresApi(Build.VERSION_CODES.P)
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

//        Logger.d("toolbar height: ${toolbar.height}")
        
        toolbar.setOnClickListener {
            DialogFragmentSelectYearMonth.newInstance(date)
                    .show(supportFragmentManager, DialogFragmentSelectYearMonth.toString())
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false);
        supportFragmentManager.addOnBackStackChangedListener(this)

        // remove shadow
        val appBarLayout = findViewById<AppBarLayout>(R.id.app_bar)
        val stateListAnimator = StateListAnimator()
        stateListAnimator.addState(IntArray(0), ObjectAnimator.ofFloat(appBarLayout, "elevation", 0f))
        appBarLayout.stateListAnimator = stateListAnimator

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

    fun setDate(date: Date) {
        this.date = date
    }

    fun setTitle(text: String) {
        val tv = findViewById<TextView>(R.id.action_bar_title)
        tv.text = text
    }
}