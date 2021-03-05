package com.asusoft.calendar.util.recyclerview.holder.dayevent.body

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter

class DayCalendarAddEventHolder (
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        // TODO: - 삭제 로직도 만들 것
        // TODO: - 클릭 이벤트 넣기
    }
}