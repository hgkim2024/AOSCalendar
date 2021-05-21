package com.asusoft.calendar.util.extension

import android.graphics.Point
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.fragment.week.FragmentWeekPage
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.util.objects.CalculatorUtil
import com.asusoft.calendar.util.objects.ThemeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.lang.Exception

fun View.getBoundsLocation(): Point {
    val viewGroup = parent as ViewGroup
    val parentLocation = IntArray(2)
    viewGroup.getLocationOnScreen(parentLocation)

    val childLocation = IntArray(2)
    this.getLocationOnScreen(childLocation)
    return Point(childLocation[0] - parentLocation[0], childLocation[1] - parentLocation[1])
}

fun View.removeFromSuperView() {
    // 이미 삭제된 경우 return
    val viewGroup = parent as? ViewGroup ?: return

    if (viewGroup.childCount > 0) {
        viewGroup.removeView(this)
    }
}

fun ConstraintLayout.addBottomSeparator(margin: Float, color: Int? = null, height: Float = 0.7F) {
    val height = CalculatorUtil.dpToPx(height)
    val margin = CalculatorUtil.dpToPx(margin)

    val separator = View(context)

    if(color == null) {
        separator.setBackgroundColor(ThemeUtil.instance.separator)
    } else {
        separator.setBackgroundColor(color)
    }

    separator.id = View.generateViewId()
    separator.layoutParams = ConstraintLayout.LayoutParams(
            0,
            height
    )

    addView(separator)

    val set = ConstraintSet()
    set.clone(this)

    set.connect(separator.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, margin)
    set.connect(separator.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, margin)
    set.connect(separator.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

    set.applyTo(this)
}

fun ConstraintLayout.addTopSeparator(margin: Float, color: Int? = null, height: Float = 0.7F) {
    val height = CalculatorUtil.dpToPx(height)
    val margin = CalculatorUtil.dpToPx(margin)

    val separator = View(context)

    if(color == null) {
        separator.setBackgroundColor(ThemeUtil.instance.separator)
    } else {
        separator.setBackgroundColor(color)
    }

    separator.id = View.generateViewId()

    separator.layoutParams = ConstraintLayout.LayoutParams(
            0,
            height
    )

    addView(separator)

    val set = ConstraintSet()
    set.clone(this)

    set.connect(separator.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, margin)
    set.connect(separator.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
    set.connect(separator.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

    set.applyTo(this)
}