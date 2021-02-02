package com.asusoft.calendar.fragment.month

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.*
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.ActivityAddEvent
import com.asusoft.calendar.activity.ActivityStart
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class FragmentMonthViewPager: Fragment() {

    private lateinit var adapter: AdapterMonthCalendar
    private lateinit var viewPager: ViewPager2
    private lateinit var todayLayout: TextView
    private var selectedDate = Date().getToday()
    private var curPageDate = Date().getToday()

    companion object {
        fun newInstance(): FragmentMonthViewPager {
            return FragmentMonthViewPager()
        }
    }

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

//        view.btn_float.setOnClickListener {
//            viewPager.setCurrentItem(viewPager.currentItem + 1, true)
//        }

        val floatingBtn = view.findViewById<FloatingActionButton>(R.id.btn_float)
        floatingBtn.setOnClickListener {
            val intent = Intent(context, ActivityAddEvent::class.java)
            intent.putExtra("date", selectedDate)
            startActivity(intent)
        }

        val weekHeader = view.findViewById<ConstraintLayout>(R.id.week_header)
        weekHeader.addView(MonthCalendarUIUtil.getWeekHeader(context))

        todayLayout = view.findViewById<TextView>(R.id.tv_today)

        todayLayout.setOnClickListener {
            movePage(Date().getToday())
            todayLayout.visibility = View.INVISIBLE
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewPager.adapter = adapter
        viewPager.setCurrentItem(AdapterMonthCalendar.START_POSITION, false)
        viewPager.offscreenPageLimit = 2

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                when (state) {
                    SCROLL_STATE_DRAGGING -> {
                        if (adapter.initFlag) {
                            adapter.initFlag = false
                            removeNullPageList()
                        }
                    }

                    SCROLL_STATE_IDLE -> {
                        setPageUI()

                        val diffMonth = viewPager.currentItem - AdapterMonthCalendar.START_POSITION
                        Log.d("Asu", "diffMonth: $diffMonth")
                        val event = HashMapEvent(HashMap())
                        event.map[FragmentMonthViewPager.toString()] = FragmentMonthViewPager.toString()
                        curPageDate = Date().getToday().getNextMonth(diffMonth).startOfMonth
                        event.map["date"] = curPageDate
                        GlobalBus.getBus().post(event)

                        val today = Date().getToday().startOfMonth
                        if (today != curPageDate) {
                            if (today < curPageDate) {
                                todayLayout.text = "<  오늘"
                            } else {
                                todayLayout.text = "오늘  >"
                            }
                            todayLayout.visibility = View.VISIBLE
                        } else {
                            todayLayout.visibility = View.INVISIBLE
                        }
                    }

                    SCROLL_STATE_SETTLING -> {
                        if (adapter.nullPageList.size > 3) {
                            Log.d("Asu", "SCROLL_STATE_SETTLING size: ${adapter.nullPageList.size}")
                            setPageUI()
                        }
                    }
                }


            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val fragmentMonthPage = event.map.getOrDefault(FragmentMonthPage.toString(), null)
        if (fragmentMonthPage != null) {
            selectedDate = event.map["date"] as Date
            Log.d("Asu", "selected day date: ${selectedDate.toStringDay()}")
        }

        val activityStart = event.map.getOrDefault(ActivityStart.toStringActivity(), null)
        if (activityStart != null) {
            val date = event.map["date"] as Date
            movePage(date)
        }
    }

    private fun movePage(date: Date) {
        val diffYear = date.calendarYear - curPageDate.calendarYear
        val diffMonth = date.calendarMonth - curPageDate.calendarMonth
        val diff = diffYear * 12 + diffMonth

        viewPager.setCurrentItem(viewPager.currentItem + diff, true)
    }

    fun setPageUI() {
        val list = removeNullPageList()

        val context = context!!

        for (page in list) {
            page.setAsyncPageUI(context)
        }
    }

    private fun removeNullPageList(): ArrayList<FragmentMonthPage> {
        val list = adapter.nullPageList
        for (idx in list.size - 1 downTo 0) {
            if (list[idx].monthCalendar != null) {
                list.removeAt(idx)
            }
        }

        return list
    }
}