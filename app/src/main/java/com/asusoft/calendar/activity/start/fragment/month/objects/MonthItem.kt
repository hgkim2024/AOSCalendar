package com.asusoft.calendar.activity.start.fragment.month.objects

import android.view.View
import java.util.*
import kotlin.collections.ArrayList

class MonthItem(
        val monthDate: Date,
        val monthView: View,
        val weekItemList: ArrayList<WeekItem>
        )