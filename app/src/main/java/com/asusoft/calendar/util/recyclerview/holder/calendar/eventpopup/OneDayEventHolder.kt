package com.asusoft.calendar.util.recyclerview.holder.calendar.eventpopup

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.fragment.month.FragmentMonthPage
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.copy.CopyEventDay
import com.asusoft.calendar.util.objects.CalendarUtil.getDayEventList
import com.asusoft.calendar.activity.calendar.fragment.month.MonthCalendarUiUtil
import com.asusoft.calendar.activity.calendar.fragment.week.FragmentWeekPage
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.objects.CalendarUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.startOfMonth
import com.asusoft.calendar.util.startOfWeek
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

    @SuppressLint("ClickableViewAccessibility")
    fun bind(position: Int) {
        val item = adapter.list[position]

        val edgeView = view.findViewWithTag<View?>(0)
        val textView = view.findViewWithTag<TextView?>(1)
        val checkBox = view.findViewWithTag<CheckBox?>(2)

        if (edgeView == null) return
        if (textView == null) return
        if (checkBox == null) return

//        Logger.d("bind isComplete: $isComplete")

        when (item) {
            is CopyEventDay -> {
                textView.text = item.name
                edgeView.setBackgroundColor(item.color)
                checkBox.visibility = View.VISIBLE

                val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
                val colors = intArrayOf(item.color)

                checkBox.buttonTintList = ColorStateList(states, colors)
                checkBox.alpha = 0.7F

                if (item.isComplete) {
                    edgeView.alpha = MonthCalendarUiUtil.COMPLETE_ALPHA
                    textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    textView.setTextColor(CalendarApplication.getColor(R.color.lightFont))
                    checkBox.isChecked = true
                } else {
                    edgeView.alpha = 1.0F
                    textView.paintFlags = 0
                    textView.setTextColor(CalendarApplication.getColor(R.color.font))
                    checkBox.isChecked = false
                }

                checkBox.clicks()
                        .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (
                                    !(typeObject is FragmentMonthPage
                                    || typeObject is FragmentWeekPage)
                            ) return@subscribe

                            item.updateIsCompete(checkBox.isChecked)

                            val date = when(typeObject) {
                                is FragmentMonthPage -> typeObject.eventViewDate
                                is FragmentWeekPage -> typeObject.eventViewDate
                                else -> Date()
                            }

                            adapter.list = getDayEventList(date)
                            adapter.notifyDataSetChanged()

                            if (item.startTime != item.endTime) {
                                CalendarUtil.calendarRefresh()
                            } else {
                                when(typeObject) {
                                    is FragmentMonthPage -> typeObject.refreshWeek()
                                    is FragmentWeekPage -> typeObject.refreshPage()
                                }
                            }
                        }
            }

            is String -> {
                textView.text = item
                edgeView.setBackgroundColor(CalendarApplication.getColor(R.color.holiday))
                checkBox.visibility = View.INVISIBLE
            }
        }

    }

    companion object {
        override fun toString(): String {
            return "OneDayEventHolder"
        }
    }

}