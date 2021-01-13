package com.asusoft.calendar.fragment.month

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.asusoft.calendar.util.*
import org.greenrobot.eventbus.util.ErrorDialogManager
import java.util.*

class AdapterMonthCalendar(fm: FragmentActivity) : FragmentStateAdapter(fm) {

    private var start: Long = Date().getToday().startOfMonth.time

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): FragmentMonthPage {
        val time = getItemId(position)
        return FragmentMonthPage.newInstance(time)
    }

//    override fun getItemId(position: Int): Long = Date(start).getNextMonth(position - START_POSITION).time

    override fun containsItem(itemId: Long): Boolean {
        return Date(itemId).calendarDay == 1
    }

    companion object {
        const val START_POSITION = Int.MAX_VALUE / 2
    }
}