package com.asusoft.calendar

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat

object CalendarUIUtil {
    private const val WEEK = 7
    private const val WEIGHT_SUM = 100.0F

    fun getOneWeekUI(
        context: Context,
        dayViewList: ArrayList<View>
    ): View {
        val inflater = LayoutInflater.from(context)
        val weekLayout = ConstraintLayout(context)
        val rate: Float = 1.0F / WEEK

        weekLayout.layoutParams = ConstraintLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        for(idx in 0 until WEEK) {
            val v = inflater.inflate(R.layout.item_one_week, null, false)
            v.id = View.generateViewId()
            weekLayout.addView(v)

            v.findViewById<ConstraintLayout>(R.id.day_layout)
            v.layoutParams = ConstraintLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            val set = ConstraintSet()
            set.clone(weekLayout)

            set.constrainPercentWidth(v.id, rate)
            set.connect(v.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

            when(idx) {
                0 -> {
                    set.connect(v.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                }

                else -> {
                    set.connect(v.id, ConstraintSet.START, dayViewList.last().id, ConstraintSet.END)
                }
            }

            set.applyTo(weekLayout)
            dayViewList.add(v)
        }

        return weekLayout
    }

    fun getMonthUI(
        context: Context,
        dayViewList: ArrayList<View>
    ): View {
        // TODO: - 추후에 row 입력 받도록 수정 - 계산해서 넣기
        val row = 6

        val monthLayout = LinearLayout(context)
        monthLayout.weightSum = WEIGHT_SUM
        monthLayout.orientation = LinearLayout.VERTICAL

        monthLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        for (idx in 0 until row) {
            val weekLayout = getOneWeekUI(context, dayViewList)
            monthLayout.addView(weekLayout)

            weekLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                WEIGHT_SUM / row
            )
        }

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

        for (idx in 0 until WEEK) {
            val tv = TextView(context)
            weekHeaderLayout.addView(tv)

            tv.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                WEIGHT_SUM / WEEK
            )

            when(idx % 7) {
                0 -> tv.text = "일"
                1 -> tv.text = "월"
                2 -> tv.text = "화"
                3 -> tv.text = "수"
                4 -> tv.text = "목"
                5 -> tv.text = "금"
                6 -> tv.text = "토"
            }

            tv.setTextColor(ContextCompat.getColor(context, R.color.font))
            tv.textSize = 12.0F
            tv.gravity = Gravity.CENTER
        }

        return weekHeaderLayout
    }
}