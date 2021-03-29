package com.asusoft.calendar.activity.setting.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.`object`.PreferenceKey
import com.asusoft.calendar.util.`object`.PreferenceKey.DRAG_AND_DROP_DEFAULT
import com.asusoft.calendar.util.`object`.PreferenceManager
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.setting.seekbar.SeekBarItem
import com.asusoft.calendar.util.recyclerview.holder.setting.switch.SwitchItem
import java.util.ArrayList

class FragmentMonthSetting: Fragment() {

    companion object {
        fun newInstance(): FragmentMonthSetting {
            return FragmentMonthSetting()
        }
    }

    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val context = this.context!!
        val view = inflater.inflate(R.layout.recyclerview, container, false)

        val list = ArrayList<Any>()

        list.add(
                SwitchItem(
                        PreferenceKey.MONTH_CALENDAR_DRAG_AND_DROP,
                        PreferenceManager.getBoolean(PreferenceKey.MONTH_CALENDAR_DRAG_AND_DROP, DRAG_AND_DROP_DEFAULT),
                        "이벤트 길게 눌러 이동 사용"
                )
        )

        list.add(
                SeekBarItem(
                        PreferenceKey.MONTH_CALENDAR_FONT_SIZE,
                        PreferenceManager.getInt(PreferenceKey.MONTH_CALENDAR_FONT_SIZE, PreferenceKey.MONTH_DEFAULT_FONT_SIZE.toInt()),
                        "글자 크기",
                        FragmentSetting.FONT_MIN_SIZE,
                        FragmentSetting.FONT_MAX_SIZE
                )
        )

        adapter = RecyclerViewAdapter(this, list)

        recyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        return view
    }

}