package com.asusoft.calendar.activity.calendar.activity

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.setting.activity.ActivitySetting
import com.asusoft.calendar.activity.calendar.SideMenuType
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.activity.calendar.dialog.DialogFragmentSelectYearMonth
import com.asusoft.calendar.activity.calendar.fragment.day.FragmentDayCalendar
import com.asusoft.calendar.activity.calendar.fragment.month.FragmentMonthViewPager
import com.asusoft.calendar.activity.calendar.fragment.search.FragmentRecentSearchTerms
import com.asusoft.calendar.activity.calendar.fragment.search.FragmentEventSearchResult
import com.asusoft.calendar.realm.RealmRecentSearchTerms
import com.asusoft.calendar.util.`object`.PreferenceKey
import com.asusoft.calendar.util.`object`.PreferenceManager
import com.asusoft.calendar.util.enums.RecentSearchTermsType
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.getToday
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.search.recentsearch.RecentSearchTermsHolder
import com.asusoft.calendar.util.recyclerview.holder.sidemenu.CalendarTypeHolder
import com.asusoft.calendar.util.recyclerview.holder.sidemenu.SideMenuTopHolder
import com.google.android.material.appbar.AppBarLayout
import com.jakewharton.rxbinding4.view.clicks
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.TimeUnit


class ActivityCalendar: AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    companion object {
        const val MONTH = 0
        const val Day = 1
    }

    private var date = Date().getToday()
    private var curFragmentIdx = PreferenceManager.getInt(PreferenceKey.SELECTED_CALENDAR_TYPE)
    private lateinit var drawerLayout: DrawerLayout
    private var searchView: SearchView? = null
    var fragmentRecentSearchTerms: FragmentRecentSearchTerms? = null
    var fragmentEventSearchResult: FragmentEventSearchResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalBus.register(this)
//        Logger.d("toolbar height: ${toolbar.height}")

        val tv = findViewById<TextView>(R.id.action_bar_title)
        tv.clicks()
            .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    DialogFragmentSelectYearMonth.newInstance(date)
                            .show(supportFragmentManager, DialogFragmentSelectYearMonth.toString())
                }

        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
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

    override fun onDestroy() {
        super.onDestroy()

        GlobalBus.unregister(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_start_menu, menu)
        val myActionMenuItem = menu.findItem(R.id.action_search)
        val searchView = myActionMenuItem.actionView as SearchView
        this.searchView = searchView
        searchView.maxWidth = Int.MAX_VALUE

        val tv = searchView.findViewById<EditText?>(R.id.search_src_text)
        tv?.setBackgroundColor(CalendarApplication.getColor(R.color.lightSeparator))
        tv?.hint = "이벤트 검색"
        tv?.textSize = 16.0F
        tv?.gravity = Gravity.CENTER_VERTICAL

        val underLine = searchView.findViewById<View?>(R.id.search_plate)
        underLine?.setBackgroundColor(Color.TRANSPARENT)

        val closeButton = searchView.findViewById<ImageView>(R.id.search_close_btn)
        closeButton.setOnClickListener {
//            Logger.d("closeButton setOnClickListener")
            searchView.isIconified = true
            if (fragmentEventSearchResult != null) {
                supportFragmentManager.popBackStackImmediate()
            }
            supportFragmentManager.popBackStack()
        }

        searchView.setOnSearchClickListener {
//            Logger.d("setOnSearchClickListener")
            if (fragmentRecentSearchTerms == null) {
                fragmentRecentSearchTerms = FragmentRecentSearchTerms.newInstance()
                supportFragmentManager.beginTransaction()
                        .replace(
                                R.id.fragment,
                                fragmentRecentSearchTerms!!,
                                FragmentRecentSearchTerms.toString()
                        )
                        .addToBackStack(null)
                        .commit()
            }  else {
                supportFragmentManager.popBackStack()
            }
        }

        searchView.setOnCloseListener {
//            Logger.d("setOnCloseListener")
            false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String?): Boolean {
//                Logger.d("onQueryTextSubmit")
                if (s == null) return false

                val recentSearchTerms = RealmRecentSearchTerms()

                recentSearchTerms.update(
                        s,
                        RecentSearchTermsType.EVENT.value
                )
                recentSearchTerms.insert()

                showEventSearchResult(s)
                return false
            }

            override fun onQueryTextChange(s: String?): Boolean {

                if (fragmentEventSearchResult != null) {
                    supportFragmentManager.popBackStackImmediate()
                    fragmentEventSearchResult = null
                }

                fragmentRecentSearchTerms?.refresh(s)
//                Logger.d("onQueryTextChange")
                return true
            }
        })
        return true
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

    private fun showEventSearchResult(s: String) {
        if (fragmentEventSearchResult == null) {
            fragmentEventSearchResult = FragmentEventSearchResult.newInstance(s)
            supportFragmentManager.beginTransaction()
                    .replace(
                            R.id.fragment,
                            fragmentEventSearchResult!!,
                            FragmentRecentSearchTerms.toString()
                    )
                    .addToBackStack(null)
                    .commit()
        } else {
            fragmentEventSearchResult?.refresh(s)
        }
    }

    private fun homeButtonIconChange() {
        val drawable = if (supportFragmentManager.backStackEntryCount > 0) {
            CalendarApplication.getDrawable(R.drawable.ic_baseline_arrow_back_24)
        } else {
            searchView?.clearFocus()
            val tv = searchView?.findViewById<EditText?>(R.id.search_src_text)
            tv?.setText("")
            searchView?.isIconified = true
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
        val sideMenuItemHolder = event.map.getOrDefault(CalendarTypeHolder.toString(), null)
        if (sideMenuItemHolder != null) {
            event.map.getOrDefault(ActivityCalendar.toString(), null) ?: return
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
            return
        }

        val recentSearchTermsHolder = event.map.getOrDefault(RecentSearchTermsHolder.toString(), null)
        if (recentSearchTermsHolder != null) {
            val s = event.map.getOrDefault("name", null) as? String
            if (s != null) {
                showEventSearchResult(s)
            }
            return
        }

        val sideMenuTopHolder = event.map.getOrDefault(SideMenuTopHolder.toString(), null)
        if (sideMenuTopHolder != null) {
            val intent = Intent(baseContext, ActivitySetting::class.java)
            startActivity(intent)
            return
        }

        val activitySetting = event.map.getOrDefault(ActivitySetting.toString(), null)
        if (activitySetting != null) {
            GlobalScope.async(Dispatchers.Main) {
                delay(300)
                changeRootFragment()
            }
            return
        }
    }
}