package com.asusoft.calendar.util.`object`

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.asusoft.calendar.fragment.month.WeekOfDayType
import com.asusoft.calendar.fragment.month.objects.MonthItem
import com.asusoft.calendar.fragment.month.objects.WeekItem
import com.asusoft.calendar.realm.RealmEventMultiDay
import com.asusoft.calendar.realm.RealmEventOneDay
import com.asusoft.calendar.util.*
import java.util.*
import kotlin.collections.ArrayList

object MonthCalendarUIUtil {
    public const val WEEK = 7
    private const val WEIGHT_SUM = 100.0F

    public const val FONT_SIZE = 12F


    // TODO: - 테스트 필요
    fun getEventOrderList(
        weekDate: Date,
        realmEventMultiDayList: List<RealmEventMultiDay>,
        realmEventOneDayList: List<RealmEventOneDay>
    ): HashMap<Long, Int> {
        val orderMap = HashMap<Long, Int>()
        val dayCheckList = java.util.ArrayList<Array<Boolean>>()

        for (eventMultiDay in realmEventMultiDayList) {
            val startOfWeek = if (eventMultiDay.startTime < weekDate.startOfWeek.time) {
                weekDate.startOfWeek.weekOfDay
            } else {
                Date(eventMultiDay.startTime).weekOfDay
            }

            val endOfWeek = if (eventMultiDay.endTime < weekDate.endOfWeek.time) {
                Date(eventMultiDay.endTime).weekOfDay
            } else {
                weekDate.endOfWeek.weekOfDay
            }

            loop@ while(true) {
                var index = 0
                val size = endOfWeek - startOfWeek + 1

                for (i in 0 until WEEK - size) {
                    if (dayCheckList.size <= index) {
                        dayCheckList.add(arrayOf(false, false, false, false, false, false, false))
                    }

                    for(j in 0 until size) {
                        if (!dayCheckList[index][i + j]) {
                            if (j == size -1) {
                                orderMap[eventMultiDay.key] = index

                                for(j in 0 until size) {
                                    dayCheckList[index][i + j] = true
                                }

                                break@loop
                            }
                        } else {
                            break
                        }
                    }
                }

                index++
            }
        }

        for (eventOneDay in realmEventOneDayList) {
            val weekOfDay = Date(eventOneDay.time).weekOfDay

            var index = 0
            loop@ while(true) {
                if (dayCheckList.size <= index) {
                    dayCheckList.add(arrayOf(false, false, false, false, false, false, false))
                }

                if (!dayCheckList[index][weekOfDay]) {
                    orderMap[eventOneDay.key] = index
                    dayCheckList[index][weekOfDay] = true

                    break@loop
                }

                index++
            }
        }

        return orderMap
    }

    private fun getOneWeekUI(
            context: Context,
            startOfWeekDate: Date,
            currentMonthDate: Date
    ): WeekItem {
        val weekLayout = ConstraintLayout(context)
        val rate: Float = 1.0F / WEEK
        var date = startOfWeekDate

        val dayViewList = ArrayList<View>()

        weekLayout.layoutParams = ConstraintLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        for(idx in 0 until WEEK) {
            val tv = TextView(context)
            tv.id = View.generateViewId()
            weekLayout.addView(tv)

            tv.text = date.calendarDay.toString()
            tv.setTextColor(WeekOfDayType.fromInt(date.weekOfDay).getFontColor(context))
            tv.textSize = FONT_SIZE
            tv.setTypeface(tv.typeface, Typeface.BOLD)

            val padding = CalculatorUtil.dpToPx(context, 8.0F)
            tv.setPadding(padding, padding, 0, 0)

            tv.layoutParams = ConstraintLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            val set = ConstraintSet()
            set.clone(weekLayout)

            set.constrainPercentWidth(tv.id, rate)
            set.connect(tv.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

            when(idx) {
                0 -> set.connect(tv.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                else -> set.connect(tv.id, ConstraintSet.START, dayViewList.last().id, ConstraintSet.END)
            }

            set.applyTo(weekLayout)
            dayViewList.add(tv)

            if (currentMonthDate.calendarMonth != date.calendarMonth) {
                tv.alpha = 0.4F
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

            val multiDayList = RealmEventMultiDay.select(weekItem.weekDate)
            val oneDayList = RealmEventOneDay.select(weekItem.weekDate)
            val orderMap = getEventOrderList(weekItem.weekDate, multiDayList, oneDayList)

            for (multiDay in multiDayList) {
                val order = orderMap.getOrDefault(multiDay.key, -1)
                if(order != -1) {
                    weekItem.addEventUI(
                            context,
                            multiDay.startTime,
                            multiDay.endTime,
                            false,
                            order
                    )
                }
            }

            for (oneDay in oneDayList) {
                val order = orderMap.getOrDefault(oneDay.key, -1)
                if(order != -1) {
                    weekItem.addEventUI(
                            context,
                            oneDay.time,
                            oneDay.time,
                            true,
                            order
                    )
                }
            }

//            weekItem.addEventUI(context, WeekOfDayType.SUNDAY, WeekOfDayType.SATURDAY, false, 0)
//            weekItem.addEventUI(context, WeekOfDayType.SUNDAY, WeekOfDayType.SATURDAY, false, 1)
//            weekItem.addEventUI(context, WeekOfDayType.SUNDAY, WeekOfDayType.SUNDAY, true, 2)
//            weekItem.addEventUI(context, WeekOfDayType.SUNDAY, WeekOfDayType.SUNDAY, true, 3)

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