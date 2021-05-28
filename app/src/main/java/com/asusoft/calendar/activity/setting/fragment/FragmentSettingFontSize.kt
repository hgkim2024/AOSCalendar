package com.asusoft.calendar.activity.setting.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.setting.activity.ActivitySetting
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.setting.text.TextItem
import java.util.ArrayList

class FragmentSettingFontSize: Fragment() {

    companion object {
        fun newInstance(): FragmentSettingFontSize {
            return FragmentSettingFontSize()
        }
    }

    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val context = this.requireContext()
        val view = inflater.inflate(R.layout.recyclerview, container, false)

        val list = ArrayList<Any>()

        list.add(
                TextItem(
                        "상단 바",
                        false
                )
        )

        list.add(SettingFontSizeType.CALENDAR_HEADER.getSeekBarItem())



        list.add(
                TextItem(
                        "월간 캘린더",
                        false
                )
        )
        list.add(SettingFontSizeType.MONTH_HEADER.getSeekBarItem())
        list.add(SettingFontSizeType.MONTH_DAY.getSeekBarItem())
        list.add(SettingFontSizeType.MONTH_ITEM.getSeekBarItem())
        list.add(SettingFontSizeType.MONTH_COUNTER.getSeekBarItem())


        list.add(
                TextItem(
                        "주간 캘린더",
                        false
                )
        )
        list.add(SettingFontSizeType.WEEK_HEADER.getSeekBarItem())
        list.add(SettingFontSizeType.WEEK_ITEM.getSeekBarItem())



        list.add(
                TextItem(
                        "일일 이벤트 팝업",
                        false
                )
        )
        list.add(SettingFontSizeType.DAY_HEADER.getSeekBarItem())
        list.add(SettingFontSizeType.DAY_ITEM.getSeekBarItem())

        adapter = RecyclerViewAdapter(this, list)

        recyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        return view
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as? ActivitySetting)?.setTitle("폰트 크기")
    }
}