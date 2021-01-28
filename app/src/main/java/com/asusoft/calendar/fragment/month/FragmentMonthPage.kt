package com.asusoft.calendar.fragment.month

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.ActivityAddEvent
import com.asusoft.calendar.activity.ActivityStart
import com.asusoft.calendar.fragment.month.objects.MonthItem
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.getNextDay
import com.asusoft.calendar.util.startOfMonth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

class FragmentMonthPage: Fragment() {

    lateinit var date: Date
    lateinit var monthItem: MonthItem
    lateinit var page: View

    var prevClickDayView: View? = null
    var monthCalendar: ConstraintLayout? = null
    var initFlag = false

    companion object {
        fun newInstance(time: Long, initFlag: Boolean): FragmentMonthPage {
            val f = FragmentMonthPage()
            val args = Bundle()
            args.putLong("time", time)
            args.putBoolean("initFlag", initFlag)
            f.arguments = args
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments!!
        val time = args.getLong("time")
        initFlag = args.getBoolean("initFlag", false)

        date = Date(time)

        GlobalBus.getBus().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        GlobalBus.getBus().unregister(this)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = this.context!!
        val inflater = LayoutInflater.from(context)
        page = inflater.inflate(R.layout.fragment_month_calender, null, false)

        if (initFlag) {
            setPageUI(context)
        }

        return page
    }

    override fun onResume() {
        super.onResume()

        setActionBarTitle()
        setAsyncPageUI(context!!)

        if (prevClickDayView != null) {
            for (weekItem in monthItem.WeekItemList) {
                for (idx in weekItem.dayViewList.indices) {
                    val dayView = weekItem.dayViewList[idx]
                    if (prevClickDayView == dayView) {
                        postSelectedDayDate(weekItem.weekDate.getNextDay(idx))
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val addEventActivity = event.map.getOrDefault(ActivityAddEvent.toStringActivity(), null)
        if (addEventActivity != null) {
            refreshPage()
        }
    }

    fun setAsyncPageUI(context: Context) {
        GlobalScope.async {
            page.post {
                setPageUI(context)
            }
        }
    }

    private fun setPageUI(context: Context) {
        monthCalendar = page.findViewById(R.id.month_calendar)
        if (monthCalendar?.childCount == 0) {
            monthItem = MonthCalendarUIUtil.getMonthUI(context, date.startOfMonth)
            monthCalendar?.addView(monthItem.monthView)
        }

        for (weekItem in monthItem.WeekItemList) {
            for (idx in weekItem.dayViewList.indices) {
                val dayView = weekItem.dayViewList[idx]
                dayView.setOnClickListener {
                    if (prevClickDayView != null) {
                        prevClickDayView!!.background = null
                    }

                    dayView.background = ContextCompat.getDrawable(context, R.drawable.border)
                    prevClickDayView = dayView

                    postSelectedDayDate(weekItem.weekDate.getNextDay(idx))
                }
            }
        }
    }

    private fun postSelectedDayDate(date: Date) {
        val event = HashMapEvent(HashMap())
        event.map[FragmentMonthPage.toString()] = FragmentMonthPage.toString()
        event.map["date"] = date
        GlobalBus.getBus().post(event)
    }

    private fun refreshPage() {
        removePage()
        setAsyncPageUI(context!!)
    }

    private fun removePage() {
        monthCalendar?.removeAllViews()
    }

    private fun setActionBarTitle() {
        val sdf = SimpleDateFormat("yyyy년 MM월")
        val title = sdf.format(date)

        (activity as ActivityStart).setTitle(title)
//        Log.d("Asu", "onResume date: $title")
    }
}