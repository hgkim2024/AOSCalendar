package com.asusoft.calendar.util.recyclerview.holder.addeventholder.visite

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter

class VisitHolder(
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        if (adapter.list[position] is VisitItem) {
            val item = adapter.list[position] as VisitItem

            val title = view.findViewById<TextView>(R.id.tv_title)
            title.text = item.title

            val subtitle = view.findViewById<TextView>(R.id.tv_subtitle)

            val ivPerson = view.findViewById<ImageButton>(R.id.iv_person)
            if (item.count > 0) {
                ivPerson.visibility = View.VISIBLE
                subtitle.text = item.count.toString()
            } else {
                ivPerson.visibility = View.INVISIBLE
                subtitle.text = "없음"
            }
        }
    }
}