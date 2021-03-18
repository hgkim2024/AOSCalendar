package com.asusoft.calendar.activity.start.fragment.month

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.asusoft.calendar.util.*
import java.util.*

class AdapterMonthCalendar(fm: FragmentActivity, date: Date) : FragmentStateAdapter(fm) {

    var start: Long = date.startOfMonth.time
    var initFlag = true

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): FragmentMonthPage {
//        Logger.d("createFragment position: $position")
        val time = getItemId(position)
        return FragmentMonthPage.newInstance(time, initFlag)
    }

    override fun getItemId(position: Int): Long = Date(start).getNextMonth(position - START_POSITION).time

    override fun containsItem(itemId: Long): Boolean {
        return Date(itemId).calendarDay == 1
    }

    companion object {
        const val START_POSITION = Int.MAX_VALUE / 2
    }
}