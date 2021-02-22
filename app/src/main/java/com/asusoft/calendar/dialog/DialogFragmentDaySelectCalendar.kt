package com.asusoft.calendar.dialog

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.`object`.CalculatorUtil
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil.SELECT_DAY_HEIGHT
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil.WEEK
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.selectday.SelectDayHolder
import com.asusoft.calendar.util.recyclerview.holder.selectday.SelectDayItem
import com.asusoft.calendar.util.recyclerview.snap.StartSnapHelper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList

class DialogFragmentDaySelectCalendar: DialogFragment() {

    companion object {
        const val DEFAULT_MONTH_COUNT = 20

        fun newInstance(): DialogFragmentDaySelectCalendar {
            return DialogFragmentDaySelectCalendar()
        }
    }

    private lateinit var adapter: RecyclerViewAdapter

    var selectedStartDate: Date? = null
    var selectedEndDate: Date? = null
    private var selectedIsStart: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalBus.getBus().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        GlobalBus.getBus().unregister(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = context!!

        val view = inflater.inflate(R.layout.dialog_select_day, container, false)

        val weekHeader = view.findViewById<ConstraintLayout>(R.id.week_header)
        weekHeader.addView(MonthCalendarUIUtil.getWeekHeader(context, true))

        val today = Date().getToday().startOfMonth
        val list = ArrayList<SelectDayItem>()

        val weight = DEFAULT_MONTH_COUNT / 2
        for (index in 0 until DEFAULT_MONTH_COUNT) {
            val item = SelectDayItem(today.getNextMonth(index - weight))
            list.add(item)
        }

        adapter = RecyclerViewAdapter(this, list as ArrayList<Any>)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val snapHelper = StartSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        recyclerView.scrollToPosition(weight)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val position = (recyclerView.layoutManager as LinearLayoutManager?)!!.findFirstCompletelyVisibleItemPosition()

//                Log.d("Asu", "onScrolled position: $position")

                if (-1 < position && position < 2) {
                    val list = getList(adapter.list.first() as SelectDayItem, true)
                    for (item in list) {
                        adapter.list.add(0, item)
                    }
                    adapter.notifyItemRangeInserted(0, list.size - 1)
                    recyclerView.scrollToPosition(list.size)

                } else if (position >= adapter.list.size - 2) {
                    val list = getList(adapter.list.last() as SelectDayItem, false)
                    adapter.list.addAll(list)
                    adapter.notifyDataSetChanged()
                }
            }
        })

        return view
    }

    fun getList(dayItem: SelectDayItem, isUp: Boolean): ArrayList<SelectDayItem> {
        val list = ArrayList<SelectDayItem>()

        for (index in 0 until DEFAULT_MONTH_COUNT) {
            val weight = if (isUp) -(index + 1) else index + 1
            val item = SelectDayItem(dayItem.date.getNextMonth(weight))
            list.add(item)
        }

        return list
    }

    override fun onResume() {
        super.onResume()

        val windowManager = activity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val params: WindowManager.LayoutParams = dialog?.window?.attributes ?: return

        val maxWidth = CalculatorUtil.dpToPx((SELECT_DAY_HEIGHT * WEEK) + 32.0F + 16.0F)
        params.width = (size.x * 0.9).toInt()
        if (params.width > maxWidth) {
            params.width = maxWidth
        }

        val maxHeight = CalculatorUtil.dpToPx(400.0F)
        params.height = (size.y * 0.9).toInt()
        if (params.height > maxHeight) {
            params.height = maxHeight
        }

        dialog?.window?.attributes = params
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val selectDayHolder = event.map.getOrDefault(SelectDayHolder.toString(), null)
        if (selectDayHolder != null) {
            val date = event.map["date"] as Date

            Log.d("Asu", "selectDayHolder received date: ${date.toStringDay()}")

            if (selectedIsStart) {
                selectedStartDate = date
                selectedIsStart = false
                selectedEndDate = null
            } else {
                if (date == selectedStartDate) return
                selectedEndDate = date

                if (date < selectedStartDate) {
                    selectedEndDate = selectedStartDate
                    selectedStartDate = date
                }
                selectedIsStart = true
            }

            adapter.notifyDataSetChanged()
        }
    }
}