package com.asusoft.calendar.util.recyclerview.holder.addeventholder.delete

import android.content.Context
import android.graphics.*
import android.os.Build
import android.view.View
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication.Companion.THROTTLE
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.objects.ThemeUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.HashMap
import java.util.concurrent.TimeUnit

class DeleteHolder (
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position]

        if (item is DeleteItem) {
            val deleteBtn = view.findViewById<ImageButton>(R.id.delete_button)

            view.clicks()
                .throttleFirst(THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    deleteItem(item)
                }

            deleteBtn.colorFilter = PorterDuffColorFilter(ThemeUtil.instance.colorAccent, PorterDuff.Mode.SRC_IN)
            deleteBtn.setBackgroundColor(Color.TRANSPARENT)

            deleteBtn.clicks()
                .throttleFirst(THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    deleteItem(item)
                }
        }
    }

    private fun deleteItem(item: DeleteItem) {
        val event = HashMapEvent(HashMap())
        event.map[DeleteHolder.toString()] = DeleteHolder.toString()
        event.map["key"] = item.EventKey
        GlobalBus.post(event)
    }

    companion object {
        override fun toString(): String {
            return "DeleteHolder"
        }
    }

}