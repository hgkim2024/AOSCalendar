package com.asusoft.calendar.activity.start.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.start.activity.ActivityStart
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter

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

    var searchText = ""

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: RecyclerViewAdapter
    lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments!!
        searchText = args.getString("searchText", "")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = this.context!!
        val view = inflater.inflate(R.layout.recyclerview, container, false)

        // TODO: - 검색 필터 기능 추가
        val list = RealmEventDay.selectCopyList(searchText)
        adapter = RecyclerViewAdapter(this, list as ArrayList<Any>)

        recyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        tvEmpty = view.findViewById<TextView>(R.id.tv_empty)
        isEmpty()

        return view
    }

    fun refresh() {
        // TODO: - 검색 필터 기능 추가
        val list = RealmEventDay.selectCopyList(searchText)
        adapter.list = list as ArrayList<Any>
        adapter.notifyDataSetChanged()
        isEmpty()
    }

    private fun isEmpty() {
        if (adapter.list.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "검색 결과가 없습니다."
        } else {
            tvEmpty.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as? ActivityStart)?.fragmentEventSearchResult = null
    }

}