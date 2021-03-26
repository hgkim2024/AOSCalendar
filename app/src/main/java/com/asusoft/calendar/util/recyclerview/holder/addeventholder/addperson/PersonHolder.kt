package com.asusoft.calendar.util.recyclerview.holder.addeventholder.addperson

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.realm.copy.CopyVisitPerson
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter

class PersonHolder(
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position]

        if (item !is CopyVisitPerson) return

        val tv = view.findViewById<TextView>(R.id.title)
        tv.text = item.name
    }

}