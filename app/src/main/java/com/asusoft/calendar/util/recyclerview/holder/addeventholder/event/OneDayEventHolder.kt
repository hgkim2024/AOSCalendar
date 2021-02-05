package com.asusoft.calendar.util.recyclerview.holder.addeventholder.event

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.realm.copy.CopyEventMultiDay
import com.asusoft.calendar.realm.copy.CopyEventOneDay
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.startOfMonth
import java.util.*

class OneDayEventHolder(
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position]

        var key = 0L
        var date = Date()

        when(item) {
            is CopyEventOneDay -> {
                key = item.key
                date = Date(item.time).startOfMonth
            }
            is CopyEventMultiDay -> {
                key = item.key
                date = Date(item.startTime).startOfMonth
            }
        }

        view.setOnClickListener {
            val event = HashMapEvent(HashMap())
            event.map[OneDayEventHolder.toString()] = OneDayEventHolder.toString()
            event.map["key"] = key
            event.map["date"] = date
            GlobalBus.getBus().post(event)
        }
    }

    companion object {
        override fun toString(): String {
            return "OneDayEventHolder"
        }
    }

}