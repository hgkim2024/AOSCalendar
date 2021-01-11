package com.asusoft.calendar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: - 실제 기기에서 적용되는지 테스트해보기 - 안드로이드 10 기기가 없음
//        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
//            setTheme(R.style.DarkTheme);
//        } else {
//            setTheme(R.style.LightTheme);
//        }

        setContentView(getMonthUI(baseContext))


    }


    fun getOneWeekUI(
        context: Context,
        dayViewList: ArrayList<View>
    ): View {
        val inflater = LayoutInflater.from(context)
        val weekLayout = ConstraintLayout(context)
        val week = 7
        val rate: Float = 1.0F / week

        weekLayout.layoutParams = ConstraintLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        for(idx in 0 until week) {
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

    fun getMonthUI(context: Context): View {
        val dayViewList = ArrayList<View>()
        val row = 6
        val weightSum = 100.0F

        val monthLayout = LinearLayout(context)
        monthLayout.weightSum = weightSum
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
                weightSum / row
            )
        }

        return monthLayout
    }

}