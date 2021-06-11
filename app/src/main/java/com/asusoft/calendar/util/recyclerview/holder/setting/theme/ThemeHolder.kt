package com.asusoft.calendar.util.recyclerview.holder.setting.theme

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.realm.copy.CopyTheme
import com.asusoft.calendar.util.extension.addClickEffect
import com.asusoft.calendar.util.objects.ThemeUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter

class ThemeHolder (
    val context: Context,
    val view: View,
    private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position] as? CopyTheme ?: return

        val title = view.findViewById<TextView>(R.id.text)
        title.text = item.name
        title.setTextColor(ThemeUtil.instance.font)
        view.addClickEffect()
    }

}