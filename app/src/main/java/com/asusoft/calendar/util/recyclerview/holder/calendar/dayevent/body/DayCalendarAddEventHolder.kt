package com.asusoft.calendar.util.recyclerview.holder.calendar.dayevent.body

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.startOfDay
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit

class DayCalendarAddEventHolder (
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val date = adapter.list[position] as? Date ?: return

        view.clicks()
            .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val item = RealmEventDay()
                item.update(
                        "",
                        date.startOfDay.time,
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