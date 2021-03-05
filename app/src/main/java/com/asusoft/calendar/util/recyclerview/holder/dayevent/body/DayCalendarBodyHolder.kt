package com.asusoft.calendar.util.recyclerview.holder.dayevent.body

import android.content.Context
import android.graphics.Paint
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.RealmEventMultiDay
import com.asusoft.calendar.realm.RealmEventOneDay
import com.asusoft.calendar.realm.copy.CopyEventMultiDay
import com.asusoft.calendar.realm.copy.CopyEventOneDay
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import java.util.*
import kotlin.collections.ArrayList

class DayCalendarBodyHolder (
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position] as DayCalendarBodyItem

        var name = ""
        var isComplete = false

        val event = item.event
        when(event) {
            is CopyEventOneDay -> {
                name = event.name
                isComplete = event.isComplete
            }
            is CopyEventMultiDay -> {
                name = event.name
                isComplete = event.isComplete
            }
        }

        val checkBox = view.findViewById<CheckBox>(R.id.checkbox)
        val editText = view.findViewById<EditText>(R.id.tv_edit)
        item.editText = editText

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

        checkBox.setOnClickListener {
            when(event) {
                is CopyEventOneDay -> event.updateIsCompete(!event.isComplete)
                is CopyEventMultiDay -> event.updateIsCompete(!event.isComplete)
            }

            val itemList = MonthCalendarUIUtil.getDayEventList(item.date, false)
            val list = getDayCalendarItemList(itemList, item.date)

            for (i in adapter.list) {
                if (i is DayCalendarBodyItem) {
                    for (j in adapter.list) {
                        if (j is DayCalendarBodyItem) {
                            i.editText?.removeTextChangedListener(j.textWatcher)
                        }
                    }
                }
            }

            adapter.list = list

            // TODO: - post 날리기 - 좀 더 생각해보고 결정
            when(event) {
                is CopyEventOneDay -> adapter.notifyDataSetChanged()
                is CopyEventMultiDay -> adapter.notifyDataSetChanged()
            }
        }

        editText.setText(name)
        for (item in adapter.list) {
            if (item is DayCalendarBodyItem) {
                editText.removeTextChangedListener(item.textWatcher)
            }
        }

        editText.addTextChangedListener(item.textWatcher)
    }

    //    itemList: ArrayList<Any>, date: Date
    private fun addEventItem(list: ArrayList<Any>, date: Date) {
        var isCompleteCount = 0

        for (idx in list.indices) {

            when(val item = list[idx]) {
                is DayCalendarBodyItem -> {
                    when (val event = item.event) {
                        is CopyEventOneDay -> if (event.isComplete) break
                        is CopyEventMultiDay -> if (event.isComplete) break
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
}