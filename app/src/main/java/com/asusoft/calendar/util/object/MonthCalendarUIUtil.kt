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
import com.asusoft.calendar.util.*
import java.util.*
import kotlin.collections.ArrayList

object MonthCalendarUIUtil {
    private const val WEEK = 7
    private const val WEIGHT_SUM = 100.0F

    fun setCalendarDate(
            context: Context,
            currentDate: Date,
            dayViewList: ArrayList<View>
    ) {
        var date = currentDate.startOfMonth.startOfWeek
        val row = getMonthRow(currentDate)

        for (weekIdx in 0 until row) {
            for(dayIdx in 0 until WEEK) {
                val v = dayViewList[(weekIdx * 7) + dayIdx]
                val tv = v.findViewById<TextView>(R.id.title)
                tv.text = date.calendarDay.toString()
                tv.setTextColor(WeekOfDayType.fromInt(date.weekOfDay).getFontColor(context))
                date = date.tomorrow
            }
            date = date.nextWeek
        }
    }

    fun getOneDay(
            context: Context,
            currentDate: Date
    ): View {
        val dayLayout = ConstraintLayout(context)
        val tv = TextView(context)
        tv.id = View.generateViewId()

        dayLayout.addView(tv)
        val tvLayout = ConstraintLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val widthMargin = CalculatorUtil.dpToPx(context, 10.0F)
        tvLayout.setMargins(widthMargin, 0, widthMargin, 0)
        tv.layoutParams = tvLayout
        tv.textSize = 12.0F
        tv.text = currentDate.calendarDay.toString()
        tv.setTextColor(WeekOfDayType.fromInt(currentDate.weekOfDay).getFontColor(context))

        val linearLayout = LinearLayout(context)
        linearLayout.id = View.generateViewId()

        dayLayout.addView(linearLayout)
        linearLayout.layoutParams = ConstraintLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
        )

        val set = ConstraintSet()
        set.clone(dayLayout)

        set.connect(tv.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        set.connect(tv.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(linearLayout.id, ConstraintSet.TOP, tv.id, ConstraintSet.BOTTOM)
        set.connect(linearLayout.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

        set.applyTo(dayLayout)
        return dayLayout
    }

    fun getOneWeekUI(
        context: Context,
        currentDate: Date,
        dayViewList: ArrayList<View>
    ): View {
//        val inflater = LayoutInflater.from(context)
        val weekLayout = ConstraintLayout(context)
        val rate: Float = 1.0F / WEEK
        var date = currentDate

        weekLayout.layoutParams = ConstraintLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        for(idx in 0 until WEEK) {
//            val v = inflater.inflate(R.layout.item_one_week, null, false)
            val v = getOneDay(context, date)
            v.id = View.generateViewId()
            weekLayout.addView(v)

//            val tv = v.findViewById<TextView>(R.id.title)
//            tv.text = date.calendarDay.toString()
//            tv.setTextColor(WeekOfDayType.fromInt(date.weekOfDay).getFontColor(context))

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
            date = date.tomorrow
        }

        return weekLayout
    }

    fun getMonthUI(
        context: Context,
        currentDate: Date,
        dayViewList: ArrayList<View>
    ): View {
        val start = System.currentTimeMillis()
        val row = getMonthRow(currentDate)
        var date = currentDate.startOfMonth.startOfWeek
        val monthLayout = LinearLayout(context)
        monthLayout.weightSum = WEIGHT_SUM
        monthLayout.orientation = LinearLayout.VERTICAL

        monthLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        for (idx in 0 until row) {
            val weekLayout = getOneWeekUI(context, date, dayViewList)
            monthLayout.addView(weekLayout)

            weekLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                WEIGHT_SUM / row
            )

            date = date.nextWeek
        }

        val diff = System.currentTimeMillis() - start
        Log.d("Asu", "diff: $diff")
        return monthLayout
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
            val leftPadding = CalculatorUtil.dpToPx(context, 10.0F)
            tv.setPadding(leftPadding, 0, 0, 0)

            tv.text = days[idx].getShortTitle()
            tv.setTextColor(days[idx].getFontColor(context))

            tv.textSize = 12.0F
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