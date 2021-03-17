package com.asusoft.calendar.util.recyclerview.holder.eventpopup

import android.content.Context
import android.graphics.Paint
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.fragment.month.FragmentMonthPage
import com.asusoft.calendar.realm.copy.CopyEventDay
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil.getDayEventList
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.startOfMonth
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit

class OneDayEventHolder(
        private val typeObject: Any,
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position]

        val edgeView = view.findViewWithTag<View?>(0)
        val textView = view.findViewWithTag<TextView?>(1)
        val checkBox = view.findViewWithTag<CheckBox?>(2)

        if (edgeView == null) return
        if (textView == null) return
        if (checkBox == null) return

        var key = 0L
        var date = Date()
        var isComplete = false

        if (item is CopyEventDay) {
            key = item.key
            date = Date(item.startTime).startOfMonth
            isComplete = item.isComplete
        }

//        Logger.d("bind isComplete: $isComplete")

        var isHoliday = false
        val name =
                when (item) {
                    is CopyEventDay -> item.name
                    is String -> {
                        isHoliday = true
                        item
                    }
                    else -> ""
                }

        if (isHoliday) {
            edgeView.setBackgroundColor(CalendarApplication.getColor(R.color.holiday))
            checkBox.visibility = View.INVISIBLE
        } else {
            edgeView.setBackgroundColor(CalendarApplication.getColor(R.color.colorAccent))
            checkBox.visibility = View.VISIBLE
        }

        textView.text = name

        if (isComplete) {
            edgeView.alpha = MonthCalendarUIUtil.COMPLETE_ALPHA
            textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            textView.setTextColor(CalendarApplication.getColor(R.color.lightFont))
            checkBox.isChecked = true
        } else {
            edgeView.alpha = 1.0F
            textView.paintFlags = 0
            textView.setTextColor(CalendarApplication.getColor(R.color.font))
            checkBox.isChecked = false
        }

        view.clicks()
            .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
            .subscribe {
                clickEvent(key, date)
            }

        checkBox.clicks()
            .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (typeObject !is FragmentMonthPage) return@subscribe

                when(item) {
                    is CopyEventDay -> {
                        item.updateIsCompete(!item.isComplete)
                    }

                    else -> return@subscribe
                }

                adapter.list = getDayEventList(typeObject.eventViewDate)
                adapter.notifyDataSetChanged()

                if (item.startTime != item.endTime) {
                    MonthCalendarUIUtil.calendarRefresh()
                } else {
                    typeObject.refreshWeek()
                }
            }
    }

    private fun clickEvent(key: Long, date: Date) {
        val event = HashMapEvent(HashMap())
        event.map[OneDayEventHolder.toString()] = OneDayEventHolder.toString()
        event.map["key"] = key
        event.map["date"] = date
        GlobalBus.getBus().post(event)
    }

    companion object {
        override fun toString(): String {
            return "OneDayEventHolder"
        }
    }

}