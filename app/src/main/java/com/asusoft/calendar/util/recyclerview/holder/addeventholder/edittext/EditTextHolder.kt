package com.asusoft.calendar.util.recyclerview.holder.addeventholder.edittext

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.extension.ExtendedEditText
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.jakewharton.rxbinding4.widget.textChanges
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class EditTextHolder(
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        if (adapter.list[position] is EditTextItem) {
            val item = adapter.list[position] as EditTextItem

            val tvEdit = view.findViewById<ExtendedEditText>(R.id.tv_edit)
            tvEdit.hint = item.hint
            tvEdit.clearTextChangedListeners()

            tvEdit.setText(item.context)
            tvEdit.addTextChangedListener(item.textWatcher)
//
//            tvEdit.textChanges()
//                .debounce(500, TimeUnit.MILLISECONDS)
//                .subscribeOn(Schedulers.io())
//                .subscribe { charSequence ->
//                    Logger.d("Text Change!!: ${charSequence.toString()}")
//                }
        }
    }
}