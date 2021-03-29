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
import com.asusoft.calendar.util.recyclerview.RecyclerItemClickListener
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter.Companion.CLICK_DELAY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.util.*

class FragmentSetting: Fragment() {

    companion object {
        const val FONT_MIN_SIZE = 8
        const val FONT_MAX_SIZE = 18

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

        for (type in SideMenuType.values()) {
            list.add(type)
        }
        list.removeAt(0)

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
                                    when(adapter.list[position]) {
                                        SideMenuType.MONTH -> {
                                            replaceFragment(
                                                    FragmentMonthSetting.newInstance(),
                                                    FragmentMonthSetting.toString()
                                            )
                                        }

                                        SideMenuType.DAY -> {
                                            replaceFragment(
                                                    FragmentDaySetting.newInstance(),
                                                    FragmentDaySetting.toString()
                                            )
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