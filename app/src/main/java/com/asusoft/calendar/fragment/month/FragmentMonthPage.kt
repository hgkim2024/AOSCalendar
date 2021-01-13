package com.asusoft.calendar.fragment.month

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.ActivityStart
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.startOfMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.text.SimpleDateFormat
import java.util.*

class FragmentMonthPage: Fragment() {

    lateinit var date: Date
    var dayViewList = ArrayList<View>()

    companion object {
        fun newInstance(time: Long): FragmentMonthPage {
            val f = FragmentMonthPage()
            val args = Bundle()
            args.putLong("time", time)
            f.arguments = args
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments!!
        val time = args.getLong("time")
        date = Date(time)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = this.context!!
        val view = inflater.inflate(R.layout.fragment_month_calender, container, false)

        GlobalScope.async {
            view.post {
                val monthCalendar = view.findViewById<ConstraintLayout>(R.id.month_calendar)
                monthCalendar.addView(MonthCalendarUIUtil.getMonthUI(context, date.startOfMonth, dayViewList))
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        val sdf = SimpleDateFormat("yyyy.MM")
        val title = sdf.format(date)

        (activity as ActivityStart).setTitle(title)
    }
}