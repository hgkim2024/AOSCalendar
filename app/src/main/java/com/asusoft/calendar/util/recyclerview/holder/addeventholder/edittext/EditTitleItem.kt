package com.asusoft.calendar.util.recyclerview.holder.addeventholder.edittext

import android.text.Editable
import android.text.TextWatcher
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication

class EditTitleItem(
        var context: String,
        val hint: String,
        var color: Int = CalendarApplication.getColor(R.color.colorAccent)
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