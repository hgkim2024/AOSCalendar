package com.asusoft.calendar.fragment.month

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.asusoft.calendar.util.*
import java.util.*

class AdapterMonthCalendar(fm: FragmentActivity) : FragmentStateAdapter(fm) {

    private var start: Long = Date().getToday().startOfMonth.time
    val nullPageList = ArrayList<FragmentMonthPage>()
    var initFlag = true

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): FragmentMonthPage {
        Log.d("Asu", "createFragment position: $position")
        val time = getItemId(position)
        val page = FragmentMonthPage.newInstance(time, initFlag)
        nullPageList.add(page)
        return page
    }

    override fun getItemId(position: Int): Long = Date(start).getNextMonth(position - START_POSITION).time

    override fun containsItem(itemId: Long): Boolean {
        return Date(itemId).calendarDay == 1
    }

    companion object {
        const val START_POSITION = Int.MAX_VALUE / 2
    }
}