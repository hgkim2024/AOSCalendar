package com.asusoft.calendar.util.extension

import android.graphics.Point
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.util.`object`.CalculatorUtil

fun View.getBoundsLocation(): Point {
    val viewGroup = parent as ViewGroup
    val parentLocation = IntArray(2)
    viewGroup.getLocationOnScreen(parentLocation)

    val childLocation = IntArray(2)
    this.getLocationOnScreen(childLocation)
    return Point(childLocation[0] - parentLocation[0], childLocation[1] - parentLocation[1])
}

fun View.removeFromSuperView() {
    val viewGroup = parent as ViewGroup
    if (viewGroup.childCount > 0) {
        viewGroup.removeView(this)
    }
}

fun ConstraintLayout.addSeparator(margin: Float) {
    val height = CalculatorUtil.dpToPx(0.7F)
    val margin = CalculatorUtil.dpToPx(margin)

    val separator = View(context)
    separator.setBackgroundColor(CalendarApplication.getColor(R.color.separator))
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
    set.connect(separator.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

    set.applyTo(this)
}