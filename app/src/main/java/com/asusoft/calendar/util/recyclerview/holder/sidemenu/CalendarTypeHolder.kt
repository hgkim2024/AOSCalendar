package com.asusoft.calendar.util.recyclerview.holder.sidemenu

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.SideMenuType
import com.asusoft.calendar.activity.calendar.activity.ActivityCalendar
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.HashMap
import java.util.concurrent.TimeUnit

class CalendarTypeHolder (
        private val typeObject: Any,
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position] as SideMenuType

        val tv = view.findViewById<TextView>(R.id.title)
        tv.text = item.getTitle()

        view.clicks()
            .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val event = HashMapEvent(HashMap())
                event.map[CalendarTypeHolder.toString()] = CalendarTypeHolder.toString()

                if (typeObject is ActivityCalendar) {
                    event.map[ActivityCalendar.toString()] = ActivityCalendar.toString()
                }

                event.map["type"] = item
                GlobalBus.post(event)
            }
    }

    companion object {
        override fun toString(): String {
            return "CalendarTypeHolder"
        }
    }

}