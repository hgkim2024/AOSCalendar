package com.asusoft.calendar.util.recyclerview.holder.dayevent.body

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.asusoft.calendar.realm.copy.CopyEventMultiDay
import com.asusoft.calendar.realm.copy.CopyEventOneDay
import com.asusoft.calendar.util.extension.ExtendedEditText
import java.util.*

class DayCalendarBodyItem(var date: Date, var event: Any) {
    val textWatcher: TextWatcher

    init {
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                when(event) {
                    is CopyEventOneDay -> (event as CopyEventOneDay).updateName(s.toString())
                    is CopyEventMultiDay -> (event as CopyEventMultiDay).updateName(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable) {}
        }
    }
}