package com.asusoft.calendar.util.recyclerview.holder.dayevent.body

import android.text.Editable
import android.text.TextWatcher
import com.asusoft.calendar.realm.copy.CopyEventDay
import java.util.*

class DayCalendarBodyItem(var date: Date, var event: Any) {
    val textWatcher: TextWatcher

    init {
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (event is CopyEventDay)
                    (event as CopyEventDay).updateName(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        }
    }
}