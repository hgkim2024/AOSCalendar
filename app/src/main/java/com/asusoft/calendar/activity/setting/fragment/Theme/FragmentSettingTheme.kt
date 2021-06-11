package com.asusoft.calendar.activity.setting.fragment.Theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.setting.activity.ActivitySetting
import com.asusoft.calendar.realm.RealmTheme
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import java.util.ArrayList

class FragmentSettingTheme: Fragment() {
    
    companion object {
        fun newInstance(): FragmentSettingTheme {
            return FragmentSettingTheme()
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
        val items = RealmTheme.selectAllCopy()
        for (item in items) {
            list.add(item)
        }

        adapter = RecyclerViewAdapter(this, list)

        recyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        return view
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as? ActivitySetting)?.setTitle("테마")
    }
    
}