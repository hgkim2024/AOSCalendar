package com.asusoft.calendar.fragment.month

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.ActivityStart
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.startOfMonth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.text.SimpleDateFormat
import java.util.*

class FragmentMonthPage: Fragment() {

    lateinit var date: Date
    var dayViewList = ArrayList<View>()
    lateinit var page: View
    var monthCalendar: ConstraintLayout? = null
    var initFlag = false

    companion object {
        fun newInstance(time: Long, initFlag: Boolean): FragmentMonthPage {
            val f = FragmentMonthPage()
            val args = Bundle()
            args.putLong("time", time)
            args.putBoolean("initFlag", initFlag)
            f.arguments = args
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments!!
        val time = args.getLong("time")
        initFlag = args.getBoolean("initFlag", false)

        date = Date(time)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val sdf = SimpleDateFormat("yyyy.MM")
        val title = sdf.format(date)
        Log.d("Asu", "onCreateView date: $title")

        val context = this.context!!
        val inflater = LayoutInflater.from(context)
        page = inflater.inflate(R.layout.fragment_month_calender, null, false)

        if (initFlag) {
            setPageUI(context)
        }

        return page
    }

    override fun onResume() {
        super.onResume()

        val sdf = SimpleDateFormat("yyyy.MM")
        val title = sdf.format(date)

        (activity as ActivityStart).setTitle(title)
        Log.d("Asu", "onResume date: $title")

        setPageUI(context!!)
    }

    fun setPageUI(context: Context) {
        GlobalScope.async {
            page.post {
                monthCalendar = page.findViewById(R.id.month_calendar)
                if (monthCalendar?.childCount == 0) {
                    monthCalendar?.addView(MonthCalendarUIUtil.getMonthUI(context, date.startOfMonth, dayViewList))
                }
            }
        }
    }
}