package com.asusoft.calendar.activity.calendar.fragment.month

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.*
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.activity.ActivityCalendar
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.activity.calendar.dialog.DialogFragmentSelectYearMonth
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class FragmentMonthViewPager: Fragment() {

    companion object {
        fun newInstance(
                date: Date? = null
        ): FragmentMonthViewPager {
            val f = FragmentMonthViewPager()

            val args = Bundle()
            if (date != null) {
                args.putLong("date", date.time)
            }

            f.arguments = args
            return f
        }
    }

    private lateinit var adapter: AdapterMonthCalendar
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

        GlobalBus.register(this)
    }

    override fun onStop() {
        super.onStop()

        GlobalBus.unregister(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val context = this.context!!
        val view = inflater.inflate(R.layout.fragment_view_pager, container, false)

        viewPager = view.findViewById(R.id.calendar)

        val weekHeader = view.findViewById<ConstraintLayout>(R.id.week_header)
        weekHeader.addView(MonthCalendarUiUtil.getWeekHeader(context))

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

        adapter = AdapterMonthCalendar(activity!!, date)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(AdapterMonthCalendar.START_POSITION, false)
        curPosition = AdapterMonthCalendar.START_POSITION
        viewPager.offscreenPageLimit = pageCount

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {

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
                    SCROLL_STATE_DRAGGING -> {
                        if (adapter.initFlag) {
                            adapter.initFlag = false
                        }
                    }

                    SCROLL_STATE_IDLE -> {
                        val date = Date(adapter.getItemId(curPosition))
                        scrollStateIdle(date)
                    }

                    SCROLL_STATE_SETTLING -> {}
                }

            }
        })
    }

    private fun loadPage() {
        val event = HashMapEvent(HashMap())
        event.map[FragmentMonthViewPager.toString()] = FragmentMonthViewPager.toString()
        GlobalBus.post(event)
//        Logger.d("page refresh")
    }

    private fun movePage(date: Date) {
        adapter.initFlag = true
        isMovePage = true

        val curPageDate = Date(adapter.getItemId(curPosition))

        val diffYear = date.calendarYear - curPageDate.calendarYear
        val diffMonth = date.calendarMonth - curPageDate.calendarMonth
        val diff = diffYear * 12 + diffMonth

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

    private fun scrollStateIdle(date: Date) {
        if (isMovePage) {
            isMovePage = false
        }

        isVisibleTodayView(date)

        if (activity is ActivityCalendar) {
            (activity as ActivityCalendar).setMonthDate(date)
        }
    }

    private fun isVisibleTodayView(date: Date) {
        val today = Date().getToday().startOfMonth.time
        val curTime = date.startOfMonth.time
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
        val dialogFragmentSelectYearMonth = event.map.getOrDefault(DialogFragmentSelectYearMonth.toString(), null)
        if (dialogFragmentSelectYearMonth != null) {
            val date = event.map["date"] as Date
            movePage(date)
        }
    }
}