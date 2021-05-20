package com.asusoft.calendar.util.recyclerview.holder.search.spinner

import android.view.View
import android.widget.AdapterView
import com.asusoft.calendar.util.objects.PreferenceManager

class SpinnerItem(
    val title: String,
    var selectItemPosition: Int,
    val itemList: ArrayList<String>,
    val key: String? = null
) {
    var onItemSelectedListener: AdapterView.OnItemSelectedListener

    init {
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectItemPosition = position
                if (key != null) {
                    PreferenceManager.setInt(key, selectItemPosition)
                }
            }
        }
    }
}