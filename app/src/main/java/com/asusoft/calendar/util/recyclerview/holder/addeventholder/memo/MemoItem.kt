package com.asusoft.calendar.util.recyclerview.holder.addeventholder.memo

import android.text.Editable
import android.text.TextWatcher

class MemoItem(
        var title: String,
        var context: String,
        val hint: String
) {
    var textWatcher: TextWatcher? = null

    init {
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                context = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        }
    }
}