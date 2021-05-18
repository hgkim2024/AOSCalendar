package com.asusoft.calendar.activity.calendar.activity

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.setting.activity.ActivitySetting
import com.asusoft.calendar.activity.calendar.SideMenuType
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.activity.calendar.dialog.DialogFragmentSelectYearMonth
import com.asusoft.calendar.activity.calendar.dialog.filter.DialogFragmentFilter
import com.asusoft.calendar.activity.calendar.dialog.filter.enums.DateFilterType
import com.asusoft.calendar.activity.calendar.dialog.filter.enums.SearchFilterType
import com.asusoft.calendar.activity.calendar.dialog.filter.enums.StringFilterType
import com.asusoft.calendar.activity.calendar.fragment.month.FragmentMonthViewPager
import com.asusoft.calendar.activity.calendar.fragment.search.FragmentRecentSearchTerms
import com.asusoft.calendar.activity.calendar.fragment.search.FragmentEventSearchResult
import com.asusoft.calendar.activity.calendar.fragment.week.FragmentWeekViewPager
import com.asusoft.calendar.realm.RealmRecentSearchTerms
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.objects.PreferenceKey
import com.asusoft.calendar.util.objects.PreferenceManager
import com.asusoft.calendar.util.enums.RecentSearchTermsType
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
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

    companion object;

    private var monthDate = Date().getToday()
    private var weekDate = Date().getToday()
    private var curFragmentIdx = PreferenceManager.getInt(PreferenceKey.SELECTED_CALENDAR_TYPE)
    private lateinit var drawerLayout: DrawerLayout
    private var searchView: SearchView? = null
    var fragmentRecentSearchTerms: FragmentRecentSearchTerms? = null
    var fragmentEventSearchResult: FragmentEventSearchResult? = null
    var searchType: StringFilterType = StringFilterType.EVENT_NAME
    var periodType: DateFilterType = DateFilterType.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: - 추후에 가로모드 지원 옵션 만들기
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        GlobalBus.register(this)
//        Logger.d("toolbar height: ${toolbar.height}")

        val tv = findViewById<TextView>(R.id.action_bar_title)
        tv.clicks()
            .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {

                    when(curFragmentIdx) {
                        SideMenuType.MONTH.value -> {
                            DialogFragmentSelectYearMonth
                                    .newInstance(monthDate)
                                    .show(
                                            supportFragmentManager,
                                            DialogFragmentSelectYearMonth.toString()
                                    )
                        }

                        SideMenuType.WEEK.value -> {
                            showDatePickerDialog()
                        }
                    }
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
        menuInflater.inflate(R.menu.menu_side, menu)
        val searchMenuItem = menu.findItem(R.id.action_search)
        val searchView = searchMenuItem.actionView as SearchView
        this.searchView = searchView

        val tv = searchView.findViewById<EditText?>(R.id.search_src_text)
        tv?.setBackgroundColor(CalendarApplication.getColor(R.color.lightSeparator))
        tv?.hint = "이벤트 검색"
        tv?.textSize = 16.0F
        tv?.gravity = Gravity.CENTER_VERTICAL

        val underLine = searchView.findViewById<View?>(R.id.search_plate)
        underLine?.setBackgroundColor(Color.TRANSPARENT)

        val closeButton = searchView.findViewById<ImageView>(R.id.search_close_btn)
        closeButton.clicks()
                .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    hideKeyboard()
                    GlobalScope.async(Dispatchers.Main) {
                        delay(300)
                        searchView.isIconified = true
                        resultPop()
                        supportFragmentManager.popBackStack()
                    }
                }

        tv?.setOnClickListener {
            resultPop()
        }

        tv?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                resultPop()
            }
        }

//        tv?.focusChanges()

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
                resultPop()

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
        hideKeyboard()
        GlobalScope.async(Dispatchers.Main) {
            delay(300)
            onBackPressed()
        }
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {

        when(menuItem.itemId) {
            android.R.id.home -> {
                hideKeyboard()
                if (supportFragmentManager.backStackEntryCount <= 1) {
                    drawerLayout.openDrawer(Gravity.LEFT)
                    return true
                }
            }

            R.id.filter -> {
                hideKeyboard()
                DialogFragmentFilter
                    .newInstance(
                            searchType.value,
                            periodType.value
                    )
                    .show(
                        supportFragmentManager,
                        DialogFragmentFilter.toString()
                    )
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun showEventSearchResult(s: String) {
        val tv = searchView?.findViewById<EditText?>(R.id.search_src_text)
        tv?.setText(s)
        tv?.setSelection(tv.text.length)

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
        val drawable = if (supportFragmentManager.backStackEntryCount > 1) {
            CalendarApplication.getDrawable(R.drawable.ic_baseline_arrow_back_24)
        } else {
            searchView?.clearFocus()
            val tv = searchView?.findViewById<EditText?>(R.id.search_src_text)
            tv?.setText("")
            searchView?.isIconified = true
            CalendarApplication.getDrawable(R.drawable.ic_baseline_menu_24)
        }

        drawable?.setTint(CalendarApplication.getColor(R.color.font))
        supportActionBar?.setHomeAsUpIndicator(drawable)
    }

    fun setMonthDate(date: Date) {
        this.monthDate = date
    }

    fun setWeekDate(date: Date) {
        this.weekDate = date
    }

    fun getWeekDate(): Date {
        return weekDate
    }

    fun setTitle(text: String) {
        val tv = findViewById<TextView>(R.id.action_bar_title)
        tv.text = text
    }

    fun resultPop() {
        if (fragmentEventSearchResult != null) {
            fragmentEventSearchResult = null
            supportFragmentManager.popBackStackImmediate()
        }
    }

    private fun changeRootFragment() {
        Logger.d("Fragment Count: ${supportFragmentManager.fragments.size}")

        if (supportFragmentManager.fragments.size > 0) {
            supportFragmentManager.popBackStack(
                    supportFragmentManager.getBackStackEntryAt(0).id,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }

        for (fragment in supportFragmentManager.fragments) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }

        when(curFragmentIdx) {
            SideMenuType.MONTH.value -> {
//                Logger.d("change fragment date: ${date.toStringDay()}")
                supportFragmentManager.beginTransaction()
                    .add(
                        R.id.fragment,
                        FragmentMonthViewPager.newInstance(monthDate),
                        FragmentMonthViewPager.toString()
                    ).addToBackStack(null).commit()
            }

            SideMenuType.WEEK.value -> {
//                Logger.d("change fragment date: ${weekDate.toStringDay()}")
                supportFragmentManager.beginTransaction()
                    .add(
                        R.id.fragment,
                            FragmentWeekViewPager.newInstance(weekDate),
                            FragmentWeekViewPager.toString()
                    ).addToBackStack(null).commit()
            }
        }

        PreferenceManager.setInt(PreferenceKey.SELECTED_CALENDAR_TYPE, curFragmentIdx)
    }

    private fun showDatePickerDialog() {

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val dateString = "$year-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
            weekDate = weekDate.stringToDate(dateString)

            val event = HashMapEvent(HashMap())
            event.map[ActivityCalendar.toString()] = ActivityCalendar.toString()
            event.map["date"] = weekDate
            GlobalBus.getBus().post(event)

            Log.d("Asu", "change date: ${weekDate.toStringDay()}")
        }

        val datePickerDialog = DatePickerDialog(
                this,
                dateSetListener,
                weekDate.calendarYear,
                weekDate.calendarMonth - 1,
                weekDate.calendarDay
        )

        datePickerDialog.setCancelable(true)
        datePickerDialog.show()
    }

    private fun hideKeyboard() {
        val focusView: View? = currentFocus
        if (focusView != null) {
            val imm: InputMethodManager? = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(focusView.windowToken, 0)
            focusView.clearFocus()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val map = event.map
        val sideMenuItemHolder = map.getOrDefault(CalendarTypeHolder.toString(), null)
        if (sideMenuItemHolder != null) {
            map.getOrDefault(ActivityCalendar.toString(), null) ?: return
            val type = map.getOrDefault("type", null) as? SideMenuType

            if (type != null) {
                curFragmentIdx = when(type) {
                    SideMenuType.MONTH -> SideMenuType.MONTH.value
                    SideMenuType.WEEK -> SideMenuType.WEEK.value
                    else -> return
                }

                drawerLayout.closeDrawers()
                GlobalScope.async(Dispatchers.Main) {
                    delay(300)
                    changeRootFragment()
                }
            }
            return
        }

        val recentSearchTermsHolder = map.getOrDefault(RecentSearchTermsHolder.toString(), null)
        if (recentSearchTermsHolder != null) {
            val s = map.getOrDefault("name", null) as? String
            if (s != null) {
                showEventSearchResult(s)
            }
            return
        }

        val sideMenuTopHolder = map.getOrDefault(SideMenuTopHolder.toString(), null)
        if (sideMenuTopHolder != null) {
            val intent = Intent(baseContext, ActivitySetting::class.java)
            startActivity(intent)
            return
        }

        val activitySetting = map.getOrDefault(ActivitySetting.toString(), null)
        if (activitySetting != null) {
            GlobalScope.async(Dispatchers.Main) {
                delay(300)
                changeRootFragment()
            }
            return
        }

        val dialogFragmentFilter = map.getOrDefault(DialogFragmentFilter.toString(), null)
        if (dialogFragmentFilter != null) {
            val search = map.getOrDefault(SearchFilterType.SEARCH.getTitle(), null) as? Int
            val period = map.getOrDefault(SearchFilterType.PERIOD.getTitle(), null) as? Int

            if (search != null
                    && period != null) {
                searchType = StringFilterType.fromInt(search)
                periodType = DateFilterType.fromInt(period)
                if (fragmentEventSearchResult != null) {
                    fragmentEventSearchResult!!.refresh(fragmentEventSearchResult!!.searchText)
                }
            }
        }
    }
}