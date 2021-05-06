package com.asusoft.calendar.util.recyclerview.holder.calendar.dayevent.body

import android.content.Context
import android.graphics.Paint
import android.view.View
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.copy.CopyEventDay
import com.asusoft.calendar.util.objects.CalendarUtil
import com.asusoft.calendar.util.objects.CalendarUtil.getDayEventList
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.ExtendedEditText
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class DayCalendarBodyHolder (
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position] as? DayCalendarBodyItem ?: return

        var name = ""
        var isComplete = false

        val event = item.event

        if (event is CopyEventDay) {
            name = event.name
            isComplete = event.isComplete
        }

        val checkBox = view.findViewById<CheckBox>(R.id.checkbox)
        val editText = view.findViewById<ExtendedEditText>(R.id.tv_edit)
        editText.textSize = CalendarUtil.DAY_FONT_SIZE

        if (isComplete) {
            editText.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            editText.setTextColor(CalendarApplication.getColor(R.color.lightFont))
            checkBox.isChecked = true
        } else {
            editText.paintFlags = 0
            editText.setTextColor(CalendarApplication.getColor(R.color.font))
            checkBox.isChecked = false
        }

        checkBox.isChecked = isComplete

        checkBox.clicks()
            .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (event is CopyEventDay)
                    event.updateIsCompete(!event.isComplete)

                val itemList = getDayEventList(item.date, false)
                val list = getDayCalendarItemList(itemList, item.date)

//            logItemList(list)
                adapter.list = list

                if (event is CopyEventDay) {
                    if (event.startTime != event.endTime) {
                        val event = HashMapEvent(HashMap())
                        event.map[DayCalendarBodyHolder.toString()] = DayCalendarBodyHolder.toString()
                        GlobalBus.post(event)
                    } else {
                        adapter.notifyDataSetChanged()
                    }
                }
            }

        editText.clearTextChangedListeners()
        editText.setText(name)
        editText.addTextChangedListener(item.textWatcher)
    }

    //    itemList: ArrayList<Any>, date: Date
    private fun addEventItem(list: ArrayList<Any>, date: Date) {
        var isCompleteCount = 0

        for (idx in list.indices) {

            when(val item = list[idx]) {
                is DayCalendarBodyItem -> {
                    when (val event = item.event) {
                        is CopyEventDay -> if (event.isComplete) break
                    }
                }
            }

            isCompleteCount++
        }

        list.add(isCompleteCount, date)
    }

    private fun getDayCalendarItemList(
            itemList: ArrayList<Any>,
            date: Date
    ): ArrayList<Any> {
        val list = ArrayList<Any>()

        for (event in itemList) {
            list.add(DayCalendarBodyItem(
                    date,
                    event
            ))
        }

        addEventItem(list, date)

        return  list
    }

    private fun logItemList(list: ArrayList<Any>) {
        for (idx in list.indices) {

            when(val item = list[idx]) {
                is DayCalendarBodyItem -> {
                    when (val event = item.event) {
//                        is CopyEventDay -> Logger.d("name: ${event.name}, isComplete: ${event.isComplete}, key: ${event.key}")
                    }
                }
            }

        }
    }

    companion object {
        override fun toString(): String {
            return "DayCalendarBodyHolder"
        }
    }
}