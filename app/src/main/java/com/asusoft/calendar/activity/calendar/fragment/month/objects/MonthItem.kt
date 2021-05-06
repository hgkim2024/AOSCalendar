package com.asusoft.calendar.activity.calendar.fragment.month.objects

import android.view.View
import java.util.*
import kotlin.collections.ArrayList

class MonthItem(
        val monthDate: Date,
        val monthView: View,
        val weekOfMonthItemList: ArrayList<WeekOfMonthItem>
        )