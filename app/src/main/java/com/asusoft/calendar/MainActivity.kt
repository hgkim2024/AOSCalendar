package com.asusoft.calendar

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: - 실제 기기에서 적용되는지 테스트해보기 - 안드로이드 10 기기가 없음
//        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
//            setTheme(R.style.DarkTheme);
//        } else {
//            setTheme(R.style.LightTheme);
//        }

        setContentView(R.layout.activity_main)
//        setContentView(CalendarUIUtil.getMonthUI(baseContext, ArrayList<View>()))

        val weekHeader = findViewById<ConstraintLayout>(R.id.week_header)
        weekHeader.addView(CalendarUIUtil.getWeekHeader(baseContext))

        val monthCalendar = findViewById<ConstraintLayout>(R.id.month_calendar)
        monthCalendar.addView(CalendarUIUtil.getMonthUI(baseContext, ArrayList()))
    }
}