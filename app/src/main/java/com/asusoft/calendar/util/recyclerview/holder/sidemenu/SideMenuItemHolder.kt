package com.asusoft.calendar.util.recyclerview.holder.sidemenu

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.start.SideMenuType
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.delete.DeleteHolder
import java.util.HashMap

class SideMenuItemHolder (
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position] as SideMenuType

        val tv = view.findViewById<TextView>(R.id.title)
        tv.text = item.getTitle()

        view.setOnClickListener {
            val event = HashMapEvent(HashMap())
            event.map[SideMenuItemHolder.toString()] = SideMenuItemHolder.toString()
            event.map["type"] = item
            GlobalBus.getBus().post(event)
        }
    }

    companion object {
        override fun toString(): String {
            return "SideMenuItemHolder"
        }
    }

}