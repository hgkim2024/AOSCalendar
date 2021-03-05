package com.asusoft.calendar.util.recyclerview.holder.dayevent.body

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.realm.RealmEventOneDay
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.startOfDay
import java.util.*

class DayCalendarAddEventHolder (
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    // TODO: - 삭제 로직도 만들 것
    fun bind(position: Int) {
        val date = adapter.list[position] as Date

        view.setOnClickListener {
            val item = RealmEventOneDay()
            item.update(
                    "",
                    date.startOfDay.time,
                    false
            )
            item.insert()

            val copyItem = item.getCopy()

            adapter.list.add(position, DayCalendarBodyItem(date, copyItem))
            adapter.notifyItemInserted(position)
        }
    }
}