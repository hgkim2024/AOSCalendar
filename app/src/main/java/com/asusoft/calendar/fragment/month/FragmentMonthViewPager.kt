package com.asusoft.calendar.fragment.month

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.*
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.ActivityAddEvent
import com.asusoft.calendar.activity.ActivityStart
import com.asusoft.calendar.dialog.DialogFragmentSelectYearMonth
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.math.abs

class FragmentMonthViewPager: Fragment() {

    companion object {
        fun newInstance(): FragmentMonthViewPager {
            return FragmentMonthViewPager()
        }
    }

    private lateinit var adapter: AdapterMonthCalendar
    private lateinit var viewPager: ViewPager2
    private lateinit var todayLayout: TextView

    private val pageCount = 1

    private var selectedDate = Date().getToday()
    private var curPosition = 0
    private var isScroll = false
    private var isMovePage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalBus.getBus().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        GlobalBus.getBus().unregister(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val context = this.context!!
        val view = inflater.inflate(R.layout.fragment_view_pager, container, false)

        adapter = AdapterMonthCalendar(activity!!)
        viewPager = view.findViewById(R.id.month_calendar)

        val weekHeader = view.findViewById<ConstraintLayout>(R.id.week_header)
        weekHeader.addView(MonthCalendarUIUtil.getWeekHeader(context))

        todayLayout = view.findViewById<TextView>(R.id.tv_today)
        todayLayout.background.alpha = 200
        todayLayout.visibility = View.INVISIBLE

        todayLayout.setOnClickListener {
            isScroll = false
            val moveDate = Date().getToday()
            movePage(moveDate)
            (activity as? ActivityStart)?.setDate(moveDate)
            todayLayout.visibility = View.INVISIBLE
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val dialogFragmentSelectYearMonth = event.map.getOrDefault(DialogFragmentSelectYearMonth.toString(), null)
        if (dialogFragmentSelectYearMonth != null) {
            val date = event.map["date"] as Date
            movePage(date)
        }
    }

    private fun loadPage() {
        val event = HashMapEvent(HashMap())
        event.map[FragmentMonthViewPager.toString()] = FragmentMonthViewPager.toString()
        GlobalBus.getBus().post(event)
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
}