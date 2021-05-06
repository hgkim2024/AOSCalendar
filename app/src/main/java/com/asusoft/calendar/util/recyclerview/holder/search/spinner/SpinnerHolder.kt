package com.asusoft.calendar.util.recyclerview.holder.search.spinner

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.objects.AdapterUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter

class SpinnerHolder(
    val context: Context,
    val view: View,
    val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position] as SpinnerItem

        val title = view.findViewById<TextView>(R.id.name)
        if (item.title == "") {
            title.text = (position + 1).toString()
        } else {
            title.text = item.title
        }

        val spinner = view.findViewById<Spinner>(R.id.spinner)
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