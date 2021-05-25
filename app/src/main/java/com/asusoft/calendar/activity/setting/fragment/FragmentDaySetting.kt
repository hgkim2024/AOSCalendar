package com.asusoft.calendar.activity.setting.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import java.util.ArrayList

class FragmentDaySetting: Fragment() {

    companion object {
        fun newInstance(): FragmentDaySetting {
            return FragmentDaySetting()
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

//        list.add(
//                SeekBarItem(
//                        PreferenceKey.DAY_CALENDAR_HEADER_FONT_SIZE,
//                        PreferenceManager.getFloat(PreferenceKey.DAY_CALENDAR_HEADER_FONT_SIZE, PreferenceKey.DAY_CALENDAR_HEADER_DEFAULT_FONT_SIZE).toInt(),
//                        "글자 크기",
//                        FragmentSetting.FONT_MIN_SIZE,
//                        FragmentSetting.FONT_MAX_SIZE
//                )
//        )

        adapter = RecyclerViewAdapter(this, list)

        recyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        return view
    }

}