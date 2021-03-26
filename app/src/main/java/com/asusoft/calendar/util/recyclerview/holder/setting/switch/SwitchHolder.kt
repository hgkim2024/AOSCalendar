package com.asusoft.calendar.util.recyclerview.holder.setting.switch

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.`object`.PreferenceManager
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter

class SwitchHolder (
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position] as? SwitchItem ?: return

        val title = view.findViewById<TextView>(R.id.title)
        title.text = item.title

        val switch = view.findViewById<SwitchCompat>(R.id.switch_button)

        switch.isChecked = item.value

        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (switch.isShown) {
                item.value = isChecked
                switch.isChecked = isChecked
                PreferenceManager.setBoolean(item.key, item.value)
            }
        }
    }

}