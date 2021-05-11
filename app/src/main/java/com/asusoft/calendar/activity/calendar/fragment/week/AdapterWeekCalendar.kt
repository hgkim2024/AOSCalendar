package com.asusoft.calendar.activity.calendar.fragment.week

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.asusoft.calendar.util.*
import com.orhanobut.logger.Logger
import java.util.*

class AdapterWeekCalendar(fm: FragmentActivity, date: Date) : FragmentStateAdapter(fm) {

    var start: Long = date.startOfWeek.time
    var initFlag = true

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): FragmentWeekPage {
//        Logger.d("createFragment position: $position")
        val time = getItemId(position)
        return FragmentWeekPage.newInstance(time, initFlag)
    }

    override fun getItemId(position: Int): Long = Date(start).getNextDay((position - START_POSITION) * WeekCalendarUiUtil.WEEK).startOfWeek.time

    override fun containsItem(itemId: Long): Boolean {
        return Date(itemId).weekOfDay == 0
    }

    companion object {
        const val START_POSITION = Int.MAX_VALUE / 2
    }
}