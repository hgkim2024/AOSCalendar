package com.asusoft.calendar.util.extension

import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.widget.NumberPicker


fun NumberPicker.setDividerColor(color: Int) {
    val pickerFields = NumberPicker::class.java.declaredFields
    for (pf in pickerFields) {
        if (pf.name == "mSelectionDivider") {
            pf.isAccessible = true
            try {
                val colorDrawable = ColorDrawable(color)
//                pf[this] = colorDrawable
                pf.set(this, colorDrawable)
            } catch (e: java.lang.IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: Resources.NotFoundException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
            break
        }
    }
}