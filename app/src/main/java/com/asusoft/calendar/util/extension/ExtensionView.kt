package com.asusoft.calendar.util.extension

import android.util.TypedValue
import android.view.View
import androidx.appcompat.content.res.AppCompatResources

fun View.addClickEffect() {
    val outValue = TypedValue()
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
    background = AppCompatResources.getDrawable(context, outValue.resourceId)
    isClickable = true
    focusable = View.FOCUSABLE
}