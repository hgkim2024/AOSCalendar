package com.asusoft.calendar.util.`object`

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.asusoft.calendar.R
import com.asusoft.calendar.fragment.month.WeekOfDayType
import com.asusoft.calendar.fragment.month.objects.MonthItem
import com.asusoft.calendar.fragment.month.objects.WeekItem
import com.asusoft.calendar.util.*
import java.util.*
import kotlin.collections.ArrayList

object MonthCalendarUIUtil {
    private const val WEEK = 7
    private const val WEIGHT_SUM = 100.0F

    public const val FONT_SIZE = 12F

    fun setCalendarDate(
            currentDate: Date,
            dayViewList: ArrayList<View>
    ) {
        if (dayViewList.isEmpty()) return
        var date = currentDate.startOfMonth.startOfWeek
        val row = getMonthRow(currentDate)

        for (weekIdx in 0 until row) {
            for(dayIdx in 0 until WEEK) {
                val v = dayViewList[(weekIdx * 7) + dayIdx]
                val tv = v.findViewById<TextView>(R.id.title)
                tv.text = date.calendarDay.toString()
                date = date.tomorrow
            }
            date = date.nextWeek
        }
    }

    private fun getOneWeekUI(
            context: Context,
            startOfWeekDate: Date,
            currentMonthDate: Date
    ): WeekItem {
        val inflater = LayoutInflater.from(context)
        val weekLayout = ConstraintLayout(context)
        val rate: Float = 1.0F / WEEK
        var date = startOfWeekDate

        val dayViewList = ArrayList<View>()

        weekLayout.layoutParams = ConstraintLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        for(idx in 0 until WEEK) {
            val v = inflater.inflate(R.layout.view_monthly_one_day, null, false)
            v.id = View.generateViewId()
            weekLayout.addView(v)

            val tv = v.findViewById<TextView>(R.id.title)
            tv.text = date.calendarDay.toString()
            tv.setTextColor(WeekOfDayType.fromInt(date.weekOfDay).getFontColor(context))

            v.layoutParams = ConstraintLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            val set = ConstraintSet()
            set.clone(weekLayout)

            set.constrainPercentWidth(v.id, rate)
            set.connect(v.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

            when(idx) {
                0 -> set.connect(v.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                else -> set.connect(v.id, ConstraintSet.START, dayViewList.last().id, ConstraintSet.END)
            }

            set.applyTo(weekLayout)
            dayViewList.add(v)

            if (currentMonthDate.calendarMonth != date.calendarMonth) {
                v.alpha = 0.4F
            }

            date = date.tomorrow
        }

        return WeekItem(startOfWeekDate, weekLayout, dayViewList)
    }

    fun getMonthUI(
            context: Context,
            startOfMonthDate: Date
    ): MonthItem {
        val start = System.currentTimeMillis()

        val weekItemList = ArrayList<WeekItem>()
        val row = getMonthRow(startOfMonthDate)
        var date = startOfMonthDate.startOfWeek
        val monthLayout = LinearLayout(context)
        monthLayout.weightSum = WEIGHT_SUM
        monthLayout.orientation = LinearLayout.VERTICAL

        monthLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        for (idx in 0 until row) {
            val weekItem = getOneWeekUI(context, date, startOfMonthDate)
//            weekItem.addEventUI(context, WeekOfDayType.SUNDAY, WeekOfDayType.SATURDAY, 0)
//            weekItem.addEventUI(context, WeekOfDayType.SUNDAY, WeekOfDayType.SATURDAY, 1)
//            weekItem.addEventUI(context, WeekOfDayType.SUNDAY, WeekOfDayType.SUNDAY, 0)
//            weekItem.addEventUI(context, WeekOfDayType.SUNDAY, WeekOfDayType.SUNDAY, 1)

            val weekLayout = weekItem.weekLayout
            monthLayout.addView(weekLayout)

            weekLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                WEIGHT_SUM / row
            )

            weekItemList.add(weekItem)
            date = date.nextWeek
        }

        val diff = System.currentTimeMillis() - start
        Log.d("Asu", "getMonthUI diff: $diff")

        return MonthItem(startOfMonthDate, monthLayout, weekItemList)
    }

    fun getWeekHeader(
        context: Context
    ): View {
        val weekHeaderLayout = LinearLayout(context)
        weekHeaderLayout.weightSum = WEIGHT_SUM
        weekHeaderLayout.orientation = LinearLayout.HORIZONTAL

        weekHeaderLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        val days = WeekOfDayType.values()

        for (idx in 0 until WEEK) {
            val tv = TextView(context)
            weekHeaderLayout.addView(tv)

            tv.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                WEIGHT_SUM / WEEK
            )
            val leftPadding = CalculatorUtil.dpToPx(context, 8.0F)
            tv.setPadding(leftPadding, 0, 0, 0)

            tv.text = days[idx].getShortTitle()
            tv.setTextColor(days[idx].getFontColor(context))

            tv.textSize = FONT_SIZE
            tv.gravity = Gravity.CENTER_VERTICAL
        }

        return weekHeaderLayout
    }

    private fun getMonthRow(date: Date): Int {
        val currentDate = date.startOfMonth
        var row = 5
        var day = currentDate.weekOfDay - 1
        day += currentDate.endOfMonth.calendarDay

        if(day > WEEK * 5) {
            row = 6
        }

        return row
    }
}