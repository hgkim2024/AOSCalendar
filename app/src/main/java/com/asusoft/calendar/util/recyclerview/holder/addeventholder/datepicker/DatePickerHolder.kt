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

                var startDayItem: StartDayItem? = null
                var endDayItem: StartDayItem? = null
                var startDayItemPosition = 0
                var endDayItemPosition = 0

                for (index in adapter.list.indices) {
                    if (adapter.list[index] is StartDayItem) {
                        if (startDayItem == null) {
                            startDayItemPosition = index
                            startDayItem = adapter.list[index] as StartDayItem
                        } else {
                            endDayItemPosition = index
                            endDayItem = adapter.list[index] as StartDayItem
                        }
                    }
                }

                if(startDayItem == null || endDayItem == null) return@setOnDateChangedListener

                val curPosition = position - 1
                if (curPosition == startDayItemPosition) {
                    if (endDayItem.date < item.date) {
                        endDayItem.date = item.date
                        adapter.notifyItemChanged(endDayItemPosition)
                    }

                    startDayItem.date = item.date
                } else {
                    if (startDayItem.date > item.date) {
                        startDayItem.date = item.date
                        adapter.notifyItemChanged(startDayItemPosition)
                    }

                    endDayItem.date = item.date
                }

                adapter.notifyItemChanged(position - 1)
            }
        }
    }
}