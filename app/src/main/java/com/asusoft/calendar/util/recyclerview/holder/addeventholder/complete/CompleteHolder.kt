package com.asusoft.calendar.util.recyclerview.holder.addeventholder.complete

import android.content.Context
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter

class CompleteHolder (
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {

        val item = adapter.list[position]

        if (item is CompleteItem) {
            val switch = view.findViewById<SwitchCompat>(R.id.switch_button)
            switch.isChecked = item.isComplete

            switch.setOnCheckedChangeListener { buttonView, isChecked ->
                item.isComplete = isChecked
            }
        }

    }

}