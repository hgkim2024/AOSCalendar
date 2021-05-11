package com.asusoft.calendar.activity.calendar.fragment.week

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.activity.ActivityCalendar
import com.asusoft.calendar.activity.calendar.dialog.DialogFragmentSelectYearMonth
import com.asusoft.calendar.activity.calendar.fragment.month.enums.WeekOfDayType
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.jakewharton.rxbinding4.view.clicks
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class FragmentWeekViewPager: Fragment() {

    companion object {
        fun newInstance(
                date: Date? = null
        ): FragmentWeekViewPager {
            val f = FragmentWeekViewPager()

            val args = Bundle()
            if (date != null) {
                args.putLong("date", date.time)
            }

            f.arguments = args
            return f
        }
    }

    private lateinit var headerView: View
    private lateinit var adapter: AdapterWeekCalendar
    private lateinit var viewPager: ViewPager2
    private lateinit var todayLayout: TextView
    var date = Date().getToday()

    private val pageCount = 1

    private var curPosition = 0
    private var isScroll = false
    private var isMovePage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments!!
        val dateTime = args.getLong("date") as Long
        if (dateTime != 0L) {
            date = Date(dateTime)
        }
    }

    override fun onStart() {
        super.onStart()
        GlobalBus.getBus().register(this)
    }

    override fun onStop() {
        GlobalBus.getBus().unregister(this)
        super.onStop()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = context!!
        val view = inflater.inflate(R.layout.fragment_week_view_pager, container, false)

        val headerLayout: ConstraintLayout = view.findViewById(R.id.week_header)
        headerView = WeekCalendarUiUtil.getWeekHeader(context)
        headerLayout.addView(headerView)

        val weekDate: Date? = (activity as? ActivityCalendar)?.getWeekDate()
        if (weekDate != null) {
            setHeaderText(weekDate)
        }

        viewPager = view.findViewById(R.id.week_calendar)

        todayLayout = view.findViewById<TextView>(R.id.tv_today)
        todayLayout.background.alpha = 230
        todayLayout.visibility = View.INVISIBLE

        todayLayout.clicks()
                .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    isScroll = false
                    val moveDate = Date().getToday()
                    movePage(moveDate)
                    (activity as? ActivityCalendar)?.setMonthDate(moveDate)
                    todayLayout.visibility = View.INVISIBLE
                }

        isVisibleTodayView(date)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = AdapterWeekCalendar(activity!!, date)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(AdapterWeekCalendar.START_POSITION, false)
        curPosition = AdapterWeekCalendar.START_POSITION
        viewPager.offscreenPageLimit = pageCount

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (!isMovePage) {
                    isScroll = true
                    viewPager.isUserInputEnabled = false
                }

                curPosition = position
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
//                Logger.d("position: ${position}, positionOffset: ${positionOffset}, positionOffsetPixels: ${positionOffsetPixels}")

                if (isScroll
                        && positionOffsetPixels == 0
                ) {
                    loadPage()

                    isScroll = false
                    viewPager.isUserInputEnabled = true
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                when (state) {
                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        if (adapter.initFlag) {
                            adapter.initFlag = false
                        }
                    }

                    ViewPager2.SCROLL_STATE_IDLE -> {
                        val date = Date(adapter.getItemId(curPosition))
                        scrollStateIdle(date)
                    }

                    ViewPager2.SCROLL_STATE_SETTLING -> {}
                }

            }
        })
    }

    private fun loadPage() {
        val event = HashMapEvent(HashMap())
        event.map[FragmentWeekViewPager.toString()] = FragmentWeekViewPager.toString()
        GlobalBus.post(event)
//        Logger.d("page refresh")
    }

    private fun scrollStateIdle(date: Date) {
        if (isMovePage) {
            isMovePage = false
        }

        isVisibleTodayView(date)

        if (activity is ActivityCalendar) {
            (activity as ActivityCalendar).setWeekDate(date)
//            Logger.d("setWeekDate: ${date.toStringDay()}")
        }
    }
    
    private fun setHeaderText(date: Date) {
        val weekTypes = WeekOfDayType.values()
        for (idx in 0 until WeekCalendarUiUtil.WEEK) {
            val tv = headerView.findViewWithTag<TextView?>(idx)
            val currentDate = date.getNextDay(idx)
            
            tv.text = "${weekTypes[idx].getShortTitle()}\n(${currentDate.calendarDay})"
        }
    }

    private fun movePage(date: Date) {
        adapter.initFlag = true
        isMovePage = true

        val curPageDate = Date(adapter.getItemId(curPosition))
        val diff = ((date.startOfWeek.time - curPageDate.startOfWeek.time) / 1000 / 60 / 60 / 24 / 7).toInt()

        if (diff == 0) {
            adapter.initFlag = false
            isMovePage = false
            return
        }

        val isSmoothScroll = abs(diff) <= pageCount + 1

        viewPager.setCurrentItem(viewPager.currentItem + diff, isSmoothScroll)

        if (!isSmoothScroll) {
            adapter.initFlag = true
            isMovePage = false
            scrollStateIdle(date)
        }
    }

    private fun isVisibleTodayView(date: Date) {
        val today = Date().getToday().startOfWeek.time
        val curTime = date.startOfWeek.time
        if (today != curTime) {
            if (today < curTime) {
                todayLayout.text = "<  오늘"
            } else {
                todayLayout.text = "오늘  >"
            }
            todayLayout.visibility = View.VISIBLE
        } else {
            todayLayout.visibility = View.INVISIBLE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val fragmentWeekPage = event.map.getOrDefault(FragmentWeekPage.toString(), null)
        if (fragmentWeekPage != null) {
            val date = event.map["date"] as Date
            setHeaderText(date)
        }

        val activityCalendar = event.map.getOrDefault(ActivityCalendar.toString(), null)
        if (activityCalendar != null) {
            val date = event.map["date"] as? Date
            if (date != null) {
                movePage(date)
            }
        }
    }
}