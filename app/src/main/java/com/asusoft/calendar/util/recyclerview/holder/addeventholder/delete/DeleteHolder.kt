package com.asusoft.calendar.util.recyclerview.holder.addeventholder.delete

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import java.util.HashMap

class DeleteHolder (
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position]

        if (item is DeleteItem) {
            view.setOnClickListener {
                val event = HashMapEvent(HashMap())
                event.map[DeleteHolder.toString()] = DeleteHolder.toString()
                event.map["key"] = item.EventKey
                        GlobalBus.getBus().post(event)
            }
        }
    }

    companion object {
        override fun toString(): String {
            return "DeleteHolder"
        }
    }

}