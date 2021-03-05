package com.asusoft.calendar.util.recyclerview.holder.dayevent.body

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.realm.RealmEventOneDay
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.startOfDay
import com.orhanobut.logger.Logger
import java.util.*

class DayCalendarAddEventHolder (
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

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

            var addEventIndex = 0
            for (item in adapter.list) {
                if (item is Date) {
                    break
                }
                addEventIndex++
            }

//            Logger.d("addEventIndex: $addEventIndex")
            adapter.list.add(addEventIndex, DayCalendarBodyItem(date, copyItem))
            adapter.notifyItemInserted(addEventIndex)
        }
    }
}