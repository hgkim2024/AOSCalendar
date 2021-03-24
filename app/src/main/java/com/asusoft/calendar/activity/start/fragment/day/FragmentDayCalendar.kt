package com.asusoft.calendar.activity.start.fragment.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.start.activity.ActivityStart
import com.asusoft.calendar.activity.start.dialog.DialogFragmentSelectYearMonth
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.getNextDay
import com.asusoft.calendar.util.getToday
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.helper.StartSnapHelper
import com.asusoft.calendar.util.recyclerview.holder.dayevent.body.DayCalendarBodyHolder
import com.asusoft.calendar.util.recyclerview.holder.dayevent.header.DayCalendarHeaderItem
import com.asusoft.calendar.util.toStringMonth
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList

class FragmentDayCalendar: Fragment() {

    companion object {
        const val DEFAULT_DAY_COUNT = 10
        const val INIT_DEFAULT_DAY_COUNT = 40

        fun newInstance(
                date: Date? = null
        ): FragmentDayCalendar {
            val f = FragmentDayCalendar()

            val args = Bundle()
            if (date != null) {
                args.putLong("date", date.time)
            }

            f.arguments = args
            return f
        }
    }

    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView
    private var date = Date().getToday()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments!!
        val dateTime = args.getLong("date")
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = this.context!!
        val view = inflater.inflate(R.layout.fragment_slow_down_recycleview, container, false)

        adapter = RecyclerViewAdapter(this, getInitList(date) as ArrayList<Any>)

        recyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition(10)

        val snapHelper = StartSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstPosition = (recyclerView.layoutManager as LinearLayoutManager?)!!.findFirstCompletelyVisibleItemPosition()

//                Logger.d("onScrolled position: $firstPosition, size: ${adapter.list.size}")

                if (-1 < firstPosition && firstPosition < 2) {
                    val list = getAddList(adapter.list.first() as DayCalendarHeaderItem, true)
                    for (item in list) {
                        adapter.list.add(0, item)
                    }
                    adapter.notifyItemRangeInserted(0, list.size - 1)
                    recyclerView.scrollToPosition(list.size)
                }

                val lastPosition = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                if (lastPosition >= adapter.list.size - 2) {
                    val list = getAddList(adapter.list.last() as DayCalendarHeaderItem, false)
                    adapter.list.addAll(list)
                    adapter.notifyDataSetChanged()
                }
            }
        })

        return view
    }


    fun getAddList(dayItem: DayCalendarHeaderItem, isUp: Boolean): ArrayList<DayCalendarHeaderItem> {
        val list = ArrayList<DayCalendarHeaderItem>()

        for (index in 0 until DEFAULT_DAY_COUNT) {
            val weight = if (isUp) -(index) else index + 1

            val date = dayItem.date.getNextDay(weight)
//            Logger.d("date: ${date.toStringDay()}")

            val item = DayCalendarHeaderItem(date, ArrayList())
            list.add(item)
        }

        return list
    }

    private fun getInitList(date: Date = Date().getToday()): ArrayList<DayCalendarHeaderItem> {
        val list = ArrayList<DayCalendarHeaderItem>()
        val initDate = date.getNextDay(-10)
        for (index in 0 until INIT_DEFAULT_DAY_COUNT) {
            val date = initDate.getNextDay(index)
            val item = DayCalendarHeaderItem(date, ArrayList())
            list.add(item)
        }

        if (activity is ActivityStart) {
            (activity as ActivityStart).setTitle(date.toStringMonth())
        }

        return list
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val dialogFragmentSelectYearMonth = event.map.getOrDefault(DialogFragmentSelectYearMonth.toString(), null)
        if (dialogFragmentSelectYearMonth != null) {
            val date = event.map["date"] as Date

            adapter.list = getInitList(date) as ArrayList<Any>
            adapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(10)
        }

        val dayCalendarBodyHolder = event.map.getOrDefault(DayCalendarBodyHolder.toString(), null)
        if (dayCalendarBodyHolder != null) {
            adapter.notifyDataSetChanged()
        }

    }
}