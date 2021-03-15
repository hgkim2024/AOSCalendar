package com.asusoft.calendar.activity.start

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.*
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.dialog.DialogFragmentSelectYearMonth
import com.asusoft.calendar.fragment.day.FragmentDayCalendar
import com.asusoft.calendar.fragment.month.FragmentMonthViewPager
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.`object`.PreferenceKey
import com.asusoft.calendar.util.`object`.PreferenceManager
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.sidemenu.SideMenuItemHolder
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.TimeUnit


class ActivityStart : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    companion object {
        const val MONTH = 0
        const val Day = 1
    }

    private var date = Date().getToday()
    private var curFragmentIdx = PreferenceManager.getInt(PreferenceKey.SELECTED_CALENDAR_TYPE)
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
//        Logger.d("toolbar height: ${toolbar.height}")

        val tv = findViewById<TextView>(R.id.action_bar_title)
        tv.clicks()
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    DialogFragmentSelectYearMonth.newInstance(date)
                            .show(supportFragmentManager, DialogFragmentSelectYearMonth.toString())
                }

        drawerLayout = findViewById(R.id.drawer_layout)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.addOnBackStackChangedListener(this)
        homeButtonIconChange()

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

        if (savedInstanceState == null) {
            changeRootFragment()
        } else {
            onBackStackChanged()
        }

        val list = ArrayList<Any>()

        for (type in SideMenuType.values()) {
            list.add(type)
        }

        val adapter = RecyclerViewAdapter(this, list)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(baseContext)
    }

    override fun onStart() {
        super.onStart()

        GlobalBus.getBus().register(this)
    }

    override fun onStop() {
        super.onStop()

        GlobalBus.getBus().unregister(this)
    }

    override fun onBackStackChanged() {
        homeButtonIconChange()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {

        when(menuItem.itemId) {
            android.R.id.home -> {
                if (supportFragmentManager.backStackEntryCount == 0) {
                    drawerLayout.openDrawer(Gravity.LEFT)
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun homeButtonIconChange() {
        val drawable = if (supportFragmentManager.backStackEntryCount > 0) {
            CalendarApplication.getDrawable(R.drawable.ic_baseline_arrow_back_24)
        } else {
            CalendarApplication.getDrawable(R.drawable.ic_baseline_menu_24)
        }

        drawable?.setTint(CalendarApplication.getColor(R.color.font))
        drawable?.alpha = (255) * 9 / 10
        supportActionBar?.setHomeAsUpIndicator(drawable)
    }

    fun setDate(date: Date) {
        this.date = date
    }

    fun setTitle(text: String) {
//        supportActionBar?.setTitle(text)
        val tv = findViewById<TextView>(R.id.action_bar_title)
        tv.text = text
    }

    private fun changeRootFragment() {
        when(curFragmentIdx) {
            MONTH -> {
//                Logger.d("change fragment date: ${date.toStringDay()}")
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment,
                        FragmentMonthViewPager.newInstance(date),
                        FragmentMonthViewPager.toString()
                    ).commit()
            }

            Day -> {
//                Logger.d("change fragment date: ${date.toStringDay()}")
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment,
                        FragmentDayCalendar.newInstance(date),
                        FragmentDayCalendar.toString()
                    ).commit()
            }
        }

        PreferenceManager.setInt(PreferenceKey.SELECTED_CALENDAR_TYPE, curFragmentIdx)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val sideMenuItemHolder = event.map.getOrDefault(SideMenuItemHolder.toString(), null)
        if (sideMenuItemHolder != null) {

            val type = event.map.getOrDefault("type", null) as? SideMenuType

            if (type != null) {
                curFragmentIdx = when(type) {
                    SideMenuType.MONTH -> MONTH
                    SideMenuType.DAY -> Day
                    else -> return
                }

                changeRootFragment()
                drawerLayout.closeDrawers()
            }
        }
    }
}