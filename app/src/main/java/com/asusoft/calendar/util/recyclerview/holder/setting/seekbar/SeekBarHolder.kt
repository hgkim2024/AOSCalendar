package com.asusoft.calendar.util.recyclerview.holder.setting.seekbar

import android.content.Context
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.objects.PreferenceManager
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter

class SeekBarHolder (
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {
        val item = adapter.list[position] as? SeekBarItem ?: return

        val title = view.findViewById<TextView>(R.id.title)
        title.text = item.title

        val seekBar = view.findViewById<SeekBar>(R.id.seek_bar)
        seekBar.min = item.min
        seekBar.max = item.max
        seekBar.progress = item.value

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                item.value = seekBar.progress
                PreferenceManager.setInt(item.key, item.value)
            }

        })
    }

}