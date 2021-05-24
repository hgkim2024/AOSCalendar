package com.asusoft.calendar.util.recyclerview.holder.sidemenu

import android.content.Context
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.provider.ContactsContract
import android.view.View
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.objects.ThemeUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.HashMap
import java.util.concurrent.TimeUnit

class SideMenuTopHolder (
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val settingButton = view.findViewById<ImageButton>(R.id.setting)
        settingButton.colorFilter = PorterDuffColorFilter(ThemeUtil.instance.font, PorterDuff.Mode.SRC_IN)
        settingButton.clicks()
                .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val event = HashMapEvent(HashMap())
                    event.map[SideMenuTopHolder.toString()] = SideMenuTopHolder.toString()
                    GlobalBus.post(event)
                }
    }

    companion object {
        override fun toString(): String {
            return "SideMenuTopHolder"
        }
    }

}