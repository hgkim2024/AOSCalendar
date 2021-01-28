package com.asusoft.calendar.fragment.month

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.*
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.ActivityAddEvent
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.getToday
import com.asusoft.calendar.util.toStringDay
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class FragmentMonthViewPager: Fragment() {

    private lateinit var adapter: AdapterMonthCalendar
    private lateinit var viewPager: ViewPager2
    private var date = Date().getToday()

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
            val args = Bundle()
            intent.putExtra("date", date)
            startActivity(intent)
        }

        val weekHeader = view.findViewById<ConstraintLayout>(R.id.week_header)
        weekHeader.addView(MonthCalendarUIUtil.getWeekHeader(context))

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

                when(state) {
                    SCROLL_STATE_DRAGGING -> {
                        if (adapter.initFlag) {
                            adapter.initFlag = false
                        }
                    }

                    SCROLL_STATE_IDLE -> {
                        setPageUI()
                    }

                    SCROLL_STATE_SETTLING -> {
                        if (adapter.nullPageList.size > 3) {
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
            date = event.map["date"] as Date
            Log.d("Asu", "selected day date: ${date.toStringDay()}")
        }
    }

    fun setPageUI() {
        val list = adapter.nullPageList
        for (idx in list.size - 1 downTo 0) {
            if (list[idx].monthCalendar != null) {
                list.removeAt(idx)
            }
        }

        val context = context!!

        for (page in list) {
            page.setAsyncPageUI(context)
        }
    }
}