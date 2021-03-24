package com.asusoft.calendar.util.recyclerview.holder.recentsearch

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.copy.CopyRecentSearchTerms
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.util.HashMap
import java.util.concurrent.TimeUnit

class RecentSearchTermsHolder(
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position] as CopyRecentSearchTerms

        val tv = view.findViewById<TextView>(R.id.title)
        tv.text = item.name

        view.clicks()
                .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    GlobalScope.async {
                        delay(100)
                        val event = HashMapEvent(HashMap())
                        event.map[RecentSearchTermsHolder.toString()] = RecentSearchTermsHolder.toString()
                        event.map["name"] = item.name
                        GlobalBus.post(event)
                    }
                }
    }


    companion object {
        override fun toString(): String {
            return "RecentSearchTermsHolder"
        }
    }
}