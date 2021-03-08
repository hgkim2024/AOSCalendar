package com.asusoft.calendar.fragment.month.objects

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import java.util.*
import kotlin.collections.ArrayList

class MonthItem(
        val monthDate: Date,
        val monthView: View,
        val weekItemList: ArrayList<WeekItem>
        )