package com.asusoft.calendar.util.recyclerview.holder.addeventholder.memo

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.ExtendedEditText
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.sidemenu.SideMenuItemHolder
import java.util.HashMap

class MemoHolder(
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        if (adapter.list[position] is MemoItem) {
            val item = adapter.list[position] as MemoItem

            val tv = view.findViewById<TextView>(R.id.title)
            tv.text = item.title

            val tvEdit = view.findViewById<ExtendedEditText>(R.id.tv_edit)
            tvEdit.hint = item.hint
            tvEdit.clearTextChangedListeners()

            tvEdit.setText(item.context)
            tvEdit.addTextChangedListener(item.textWatcher)
        }
    }

    companion object {
        override fun toString(): String {
            return "MemoHolder"
        }
    }
}