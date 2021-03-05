package com.asusoft.calendar.fragment.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.getNextDay
import com.asusoft.calendar.util.getToday
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.dayevent.header.DayCalendarHeaderItem
import com.asusoft.calendar.util.recyclerview.snap.StartSnapHelper
import java.util.*
import kotlin.collections.ArrayList

class FragmentDayCalendar: Fragment() {

    companion object {
        const val DEFAULT_DAY_COUNT = 30

        fun newInstance(): FragmentDayCalendar {
            return FragmentDayCalendar()
        }
    }

    private val today = Date().getToday()
    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = this.context!!

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_recycleview, container, false)

        val list = ArrayList<DayCalendarHeaderItem>()

        for (index in 0 until DEFAULT_DAY_COUNT) {
            val date = today.getNextDay(index)
            val item = DayCalendarHeaderItem(date, ArrayList())
            list.add(item)
        }

        adapter = RecyclerViewAdapter(this, list as ArrayList<Any>)

        recyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val snapHelper = StartSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstPosition = (recyclerView.layoutManager as LinearLayoutManager?)!!.findFirstCompletelyVisibleItemPosition()

//                Logger.d("onScrolled position: $firstPosition, size: ${adapter.list.size}")

                if (-1 < firstPosition && firstPosition < 2) {
                    val list = getList(adapter.list.first() as DayCalendarHeaderItem, true)
                    for (item in list) {
                        adapter.list.add(0, item)
                    }
                    adapter.notifyItemRangeInserted(0, list.size - 1)
                    recyclerView.scrollToPosition(list.size)

                }

                val lastPosition = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                if (lastPosition >= adapter.list.size - 2) {
                    val list = getList(adapter.list.last() as DayCalendarHeaderItem, false)
                    adapter.list.addAll(list)
                    adapter.notifyDataSetChanged()
                }
            }
        })

        return view
    }


    fun getList(dayItem: DayCalendarHeaderItem, isUp: Boolean): ArrayList<DayCalendarHeaderItem> {
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
}