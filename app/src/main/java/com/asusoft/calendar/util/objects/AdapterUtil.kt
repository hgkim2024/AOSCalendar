package com.asusoft.calendar.util.objects

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.asusoft.calendar.R
import com.asusoft.calendar.application.CalendarApplication

class AdapterUtil {

    companion object {

        fun <T> getSpinnerAdapter(context: Context, spinner: Spinner, list: List<T>): ArrayAdapter<T> {
            return object: ArrayAdapter<T>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                list
            ){
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view: TextView = super.getView(
                        position,
                        convertView,
                        parent
                    ) as TextView

                    view.textAlignment = View.TEXT_ALIGNMENT_CENTER

                    return view
                }

                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    val view: TextView = super.getDropDownView(
                        position,
                        convertView,
                        parent
                    ) as TextView

                    view.setTextColor(ThemeUtil.instance.font)

                    if (position == spinner.selectedItemPosition){
                        view.setBackgroundColor(ThemeUtil.instance.colorAccent)
                        view.background.alpha = 0x4D
                    } else {
                        view.setBackgroundColor(ThemeUtil.instance.background)
                        view.background.alpha = 0xFF
                    }

                    view.textAlignment = View.TEXT_ALIGNMENT_CENTER

                    return view
                }
            }
        }
    }
}