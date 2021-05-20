package com.asusoft.calendar.activity.setting.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.SideMenuType
import com.asusoft.calendar.activity.calendar.dialog.filter.enums.SearchFilterType
import com.asusoft.calendar.util.objects.PreferenceKey
import com.asusoft.calendar.util.objects.PreferenceManager
import com.asusoft.calendar.util.recyclerview.RecyclerItemClickListener
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter.Companion.CLICK_DELAY
import com.asusoft.calendar.util.recyclerview.holder.search.spinner.SpinnerItem
import com.asusoft.calendar.util.recyclerview.holder.setting.text.TextItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.util.*

class FragmentSetting: Fragment() {

    companion object {
        fun newInstance(): FragmentSetting {
            return FragmentSetting()
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

        list.add(TextItem("폰트 크기", true))

        val orientationList = ArrayList<String>()
        orientationList.addAll(arrayOf("세로 고정", "자동"))
        list.add(
                SpinnerItem(
                        "화면 방향",
                        PreferenceManager.getInt(PreferenceKey.CALENDAR_ORIENTATION, PreferenceKey.CALENDAR_DEFAULT_ORIENTATION),
                        orientationList,
                        PreferenceKey.CALENDAR_ORIENTATION
                )
        )

        adapter = RecyclerViewAdapter(this, list)

        recyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        recyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                        context,
                        recyclerView,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View?, position: Int) {
                                GlobalScope.async(Dispatchers.Main) {
                                    delay(CLICK_DELAY)
                                    when(val item = adapter.list[position]) {
                                        is TextItem -> {
                                            when(item.text) {
                                                "폰트 크기" -> {
                                                    replaceFragment(
                                                            FragmentSettingFontSize.newInstance(),
                                                            FragmentSettingFontSize.toString()
                                                    )
                                                }
                                            }
                                        }

                                        else -> {}
                                    }
                                }
                            }

                            override fun onItemLongClick(view: View?, position: Int) {}
                        }
                )
        )

        return view
    }

    fun replaceFragment(instance: Fragment, tag: String) {
        fragmentManager!!
                .beginTransaction()
                .replace(
                        R.id.fragment,
                        instance,
                        tag
                ).addToBackStack(null).commit()
    }
}