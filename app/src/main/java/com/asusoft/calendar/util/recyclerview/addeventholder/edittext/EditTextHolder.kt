package com.asusoft.calendar.util.recyclerview.addeventholder.edittext

import android.content.Context
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter

class EditTextHolder(
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        if (adapter.list[position] is EditTextItem) {
            val item = adapter.list[position] as EditTextItem

            val tvEdit = view.findViewById<EditText>(R.id.tv_edit)
            tvEdit.hint = item.hint

            for (otherItem in adapter.list) {
                if (otherItem is EditTextItem) {
                    tvEdit.removeTextChangedListener(otherItem.textWatcher)
                }
            }

            tvEdit.setText(item.context)
            tvEdit.addTextChangedListener(item.textWatcher)
        }
    }
}