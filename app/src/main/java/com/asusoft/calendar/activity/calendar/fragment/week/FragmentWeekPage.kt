package com.asusoft.calendar.activity.calendar.fragment.week

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.asusoft.calendar.util.extension.removeFromSuperView
import com.asusoft.calendar.util.getToday
import com.asusoft.calendar.util.objects.WeekCalendarUiUtil
import com.asusoft.calendar.util.startOfWeek
import java.util.*

class FragmentWeekPage: Fragment() {

    companion object {
        fun newInstance(): FragmentWeekPage {
            return FragmentWeekPage()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = this.context!!
        val weekItem = WeekCalendarUiUtil.getOneWeekUI(context, Date().getToday().startOfWeek)
        return weekItem.rootLayout
    }
    
}