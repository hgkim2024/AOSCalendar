package com.asusoft.calendar.util.recyclerview.holder.setting.text

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.extension.addClickEffect
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter

class TextHolder (
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position] as? TextItem ?: return

        val title = view.findViewById<TextView>(R.id.text)
        title.text = item.text

        if (item.isClickable) {
            view.addClickEffect()
        }
    }

}