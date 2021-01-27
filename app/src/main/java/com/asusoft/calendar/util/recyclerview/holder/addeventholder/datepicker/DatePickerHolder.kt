package com.asusoft.calendar.util.recyclerview.holder.addeventholder.datepicker

import android.content.Context
import android.view.View
import android.widget.DatePicker
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.startday.StartDayItem
import com.asusoft.calendar.util.stringToDate

class DatePickerHolder(
    val context: Context,
    val view: View,
    private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        if (adapter.list[position] is DatePickerItem) {
            val item = adapter.list[position] as DatePickerItem

            val datePicker = view.findViewById<DatePicker>(R.id.date_picker)
            datePicker.setOnDateChangedListener { _, year, month, day ->
                val dateString = "$year-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
                item.date = item.date.stringToDate(dateString)

                if (adapter.list[position - 1] is StartDayItem) {
                    val startDayItem = adapter.list[position - 1] as StartDayItem
                    startDayItem.date = item.date
                    adapter.notifyItemChanged(position - 1)
                }
            }
        }
    }
}