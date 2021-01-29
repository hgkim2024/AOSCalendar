package com.asusoft.calendar.util.extension

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.asusoft.calendar.R
import com.asusoft.calendar.util.`object`.CalculatorUtil

fun ConstraintLayout.addSeparator(margin: Float) {
    val height = CalculatorUtil.dpToPx(0.7F)
    val margin = CalculatorUtil.dpToPx(margin)

    val separator = View(context)
    separator.setBackgroundColor(ContextCompat.getColor(context, R.color.separator))
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