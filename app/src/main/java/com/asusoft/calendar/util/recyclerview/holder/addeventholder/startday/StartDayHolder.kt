package com.asusoft.calendar.util.recyclerview.holder.addeventholder.startday

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
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
        }
    }
}