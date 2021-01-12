package com.asusoft.calendar.util.`object`

import android.content.Context
import android.util.DisplayMetrics

object CalculatorUtil {
    fun pxToDp(context: Context, px: Float): Int {
        val dpi: Float = context.resources.displayMetrics.densityDpi.toFloat()
        val density: Float = DisplayMetrics.DENSITY_DEFAULT.toFloat()
        return (px / (dpi / density)).toInt()
    }

    fun dpToPx(context: Context, dp: Float): Int {
        val dpi: Float = context.resources.displayMetrics.densityDpi.toFloat()
        val density: Float = DisplayMetrics.DENSITY_DEFAULT.toFloat()
        return (dp * (dpi / density)).toInt()
    }
}