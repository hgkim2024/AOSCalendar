package com.asusoft.calendar.util.recyclerview.holder.calendar.eventpopup

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.util.objects.ThemeUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter

class OneDayHolidayHolder(
        private val typeObject: Any,
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    @SuppressLint("ClickableViewAccessibility")
    fun bind(position: Int) {
        val item = adapter.list[position]

        val edgeView = view.findViewWithTag<View?>(0)
        val textView = view.findViewWithTag<TextView?>(1)
        val checkBox = view.findViewWithTag<CheckBox?>(2)

        if (edgeView == null) return
        if (textView == null) return
        if (checkBox == null) return

//        Logger.d("bind isComplete: $isComplete")

        when (item) {
            is String -> {
                textView.text = item
                edgeView.setBackgroundColor(ThemeUtil.instance.holiday)
                checkBox.visibility = View.INVISIBLE
            }
        }

    }


}