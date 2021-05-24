package com.asusoft.calendar.util.recyclerview.holder.search.spinner

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.objects.AdapterUtil
import com.asusoft.calendar.util.objects.ThemeUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter

class SpinnerHolder(
    val context: Context,
    val view: View,
    val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position] as SpinnerItem

        val title = view.findViewById<TextView>(R.id.name)
        title.setTextColor(ThemeUtil.instance.font)
        if (item.title == "") {
            title.text = (position + 1).toString()
        } else {
            title.text = item.title
        }

        val spinner = view.findViewById<Spinner>(R.id.spinner)

        val backgroundColor = ThemeUtil.instance.font
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            spinner.background.colorFilter = BlendModeColorFilter(backgroundColor, BlendMode.SRC_IN)
        } else {
            spinner.background.setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN)
        }

        val adapter: ArrayAdapter<String> = AdapterUtil.getSpinnerAdapter(
            context,
            spinner,
            item.itemList
        )
        spinner.adapter = adapter
        spinner.onItemSelectedListener = item.onItemSelectedListener
        spinner.setSelection(item.selectItemPosition)
    }

}