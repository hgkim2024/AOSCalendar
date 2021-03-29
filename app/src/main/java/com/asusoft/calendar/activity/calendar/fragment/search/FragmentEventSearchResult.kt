package com.asusoft.calendar.activity.calendar.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.activity.ActivityCalendar
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.orhanobut.logger.Logger

class FragmentEventSearchResult: Fragment() {

    companion object {
        fun newInstance(
                searchText: String
        ): FragmentEventSearchResult {
            val f = FragmentEventSearchResult()

            val args = Bundle()
            args.putString("searchText", searchText)
            f.arguments = args
            return f
        }
    }

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: RecyclerViewAdapter
    lateinit var tvEmpty: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = this.context!!
        val view = inflater.inflate(R.layout.recyclerview, container, false)

        // TODO: - 검색 필터 기능 추가
        val args = arguments!!
        val searchText = args.getString("searchText", "")
        val list = RealmEventDay.selectCopyList(searchText)
        adapter = RecyclerViewAdapter(this, list as ArrayList<Any>)

        recyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        tvEmpty = view.findViewById<TextView>(R.id.tv_empty)
        isEmpty()

        return view
    }

    fun refresh(s: String) {
        // TODO: - 검색 필터 기능 추가
        val list = RealmEventDay.selectCopyList(s)
        Logger.d("refresh(), list: $list")
        adapter.list = list as ArrayList<Any>
        adapter.notifyDataSetChanged()
        isEmpty()
    }

    private fun isEmpty() {
        if (adapter.list.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "검색 결과가 없습니다."
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as? ActivityCalendar)?.fragmentEventSearchResult = null
    }

}