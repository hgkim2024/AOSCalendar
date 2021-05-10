package com.asusoft.calendar.activity.calendar.fragment.week

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.activity.ActivityCalendar
import com.asusoft.calendar.activity.calendar.fragment.week.objects.WeekItem
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.recyclerview.holder.search.recentsearch.RecentSearchTermsHolder
import com.asusoft.calendar.util.startOfWeek
import com.asusoft.calendar.util.toStringMonth
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class FragmentWeekPage: Fragment() {

    companion object {
        fun newInstance(time: Long, initFlag: Boolean): FragmentWeekPage {
            val f = FragmentWeekPage()

            val args = Bundle()
            args.putLong("time", time)
            args.putBoolean("initFlag", initFlag)

            f.arguments = args
            return f
        }
    }

    private lateinit var date: Date
    private var initFlag = false

    var weekItem: WeekItem? = null
    private lateinit var page: View
    private var weekCalendar: ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments!!

        val time = args.getLong("time")
        initFlag = args.getBoolean("initFlag", false)

        date = Date(time)
        GlobalBus.register(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        GlobalBus.unregister(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = this.context!!

        page = inflater.inflate(R.layout.fragment_constraint_layout, null, false)

        if (initFlag) {
            setPageUI(context)
        }

        return page
    }

    override fun onResume() {
        super.onResume()
        val context = context!!

        setActionBarTitle()
        setAsyncPageUI(context)
        updateHeaderText()
    }

    private fun setPageUI(context: Context) {
        weekCalendar = page.findViewById(R.id.calendar)
        if (weekCalendar?.childCount == 1) {
            weekItem = WeekCalendarUiUtil.getOneWeekUI(context, date.startOfWeek)
            weekCalendar?.addView(weekItem!!.rootLayout)

//            Logger.d("weekItem!!.eventViewList: ${weekItem!!.eventViewList.size}")

            val tvEmpty = page.findViewById<TextView>(R.id.tv_empty)
            if (WeekCalendarUiUtil.isEmptyEvent(weekItem!!)) {
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.bringToFront()
            } else {
                tvEmpty.visibility = View.GONE
            }
        }
    }

    private fun setAsyncPageUI(context: Context) {
        GlobalScope.async(Dispatchers.Main) {
            setPageUI(context)
        }
    }

    private fun updateHeaderText() {
        val event = HashMapEvent(HashMap())
        event.map[FragmentWeekPage.toString()] = FragmentWeekPage.toString()
        event.map["date"] = date
        GlobalBus.post(event)
    }

    private fun setActionBarTitle() {
        if (activity is ActivityCalendar) {
            (activity as ActivityCalendar).setTitle(date.toStringMonth())
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val weekViewPager = event.map.getOrDefault(FragmentWeekViewPager.toString(), null)
        if (weekViewPager != null) {
            setAsyncPageUI(context!!)
        }
    }
}