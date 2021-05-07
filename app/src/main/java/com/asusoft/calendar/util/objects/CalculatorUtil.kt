package com.asusoft.calendar.util.objects

import android.R
import android.util.DisplayMetrics
import android.util.TypedValue
import com.asusoft.calendar.application.CalendarApplication


object CalculatorUtil {

    private lateinit var context: CalendarApplication

    fun setContext(context: CalendarApplication) {
        this.context = context
    }

    fun pxToDp(px: Float): Int {
        val dpi: Float = context.resources.displayMetrics.densityDpi.toFloat()
        val density: Float = DisplayMetrics.DENSITY_DEFAULT.toFloat()
        return (px / (dpi / density)).toInt()
    }

    fun dpToPx(dp: Float): Int {
        val dpi: Float = context.resources.displayMetrics.densityDpi.toFloat()
        val density: Float = DisplayMetrics.DENSITY_DEFAULT.toFloat()
        return (dp * (dpi / density)).toInt()
    }

    fun spToPx(sp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics).toInt()
    }

    fun getStatusBarHeight(): Int {
        var statusBarHeight = 0
        val resourceId: Int = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0)
            statusBarHeight = context.resources.getDimensionPixelSize(resourceId)

        return statusBarHeight
    }

    fun getNavigationBarHeight(): Int {
        var bottomBarHeight = 0
        val resourceIdBottom: Int = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceIdBottom > 0)
            bottomBarHeight = context.resources.getDimensionPixelSize(resourceIdBottom)

        return bottomBarHeight
    }

    fun getActionBarHeight(): Int {
        var actionBarHeight = 0

        val tv = TypedValue()
        if (
                context.theme.resolveAttribute
                (
                        R.attr.actionBarSize,
                        tv,
                        true
                )
        ) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data,
                    context.resources.displayMetrics
            )
        }

        return actionBarHeight
    }

    fun getDeviceWidth(): Int {
        return context.resources.displayMetrics.widthPixels
    }

    fun getDeviceHeight(): Int {
        return context.resources.displayMetrics.heightPixels
    }

    fun getActivityHeight(): Int {
        return getDeviceHeight() - (getStatusBarHeight() + getActionBarHeight())
    }

    fun getMonthCalendarHeight(): Int {
        return getActivityHeight() - dpToPx(30.0F)
    }
}