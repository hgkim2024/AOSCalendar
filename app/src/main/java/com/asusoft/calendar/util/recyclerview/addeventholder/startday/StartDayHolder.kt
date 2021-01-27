package com.asusoft.calendar.util.recyclerview.addeventholder.startday

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.addeventholder.datepicker.DatePickerHolder
import com.asusoft.calendar.util.recyclerview.addeventholder.datepicker.DatePickerItem
import com.asusoft.calendar.util.recyclerview.addeventholder.edittext.EditTextHolder
import com.asusoft.calendar.util.toStringDay

class StartDayHolder(
    val context: Context,
    val view: View,
    private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        if (adapter.list[position] is StartDayItem) {
            val item = adapter.list[position] as StartDayItem

            val title = view.findViewById<TextView>(R.id.tv_title)
            title.text = item.title

            val subtitle = view.findViewById<TextView>(R.id.tv_subtitle)
            subtitle.text = item.date.toStringDay()

            view.setOnClickListener {
                addDatePicker(item, position)
            }
        }
    }

    private fun addDatePicker(item: StartDayItem, position: Int) {
        for (idx in adapter.list.size -1 downTo 0) {
            val oldDatePickerItem = adapter.list.getOrNull(idx)
            if (oldDatePickerItem != null) {
                if (oldDatePickerItem is DatePickerItem) {
                    adapter.list.removeAt(idx)
                    adapter.notifyItemRemoved(idx)
                    logAdapter()
                }
            }
        }

        val newDatePickerItem = DatePickerItem(item.date)
        adapter.list.add(position + 1, newDatePickerItem)
        adapter.notifyItemInserted(position + 1)
        logAdapter()
    }

    private fun logAdapter() {
        Log.d("Asu", "size: ${adapter.list.size} =================================================")
    }
}