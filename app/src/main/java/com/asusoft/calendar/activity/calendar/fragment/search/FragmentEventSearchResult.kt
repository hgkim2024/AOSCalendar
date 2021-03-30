package com.asusoft.calendar.activity.calendar.fragment.search

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.addEvent.activity.ActivityAddEvent
import com.asusoft.calendar.activity.calendar.activity.ActivityCalendar
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.realm.copy.CopyEventDay
import com.asusoft.calendar.util.recyclerview.RecyclerItemClickListener
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

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
    private var searchText = ""
    private var position = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = this.context!!
        val view = inflater.inflate(R.layout.recyclerview, container, false)

        // TODO: - 검색 필터 기능 추가
        val args = arguments!!
        searchText = args.getString("searchText", "")
        val list = RealmEventDay.selectCopyList(searchText)
        adapter = RecyclerViewAdapter(this, list as ArrayList<Any>)

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
                                    delay(RecyclerViewAdapter.CLICK_DELAY)
                                    clickItem(position)
                                }
                            }

                            override fun onItemLongClick(view: View?, position: Int) {}
                        }
                )
        )

        tvEmpty = view.findViewById<TextView>(R.id.tv_empty)
        isEmpty()

        return view
    }

    override fun onStart() {
        refresh(searchText)
        recyclerView.scrollToPosition(position)
        super.onStart()
    }

    override fun onStop() {
        position = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
        super.onStop()
    }

    fun clickItem(position: Int) {
        val item = adapter.list[position] as CopyEventDay
        val intent = Intent(context, ActivityAddEvent::class.java)
        intent.putExtra("key", item.key)
        startActivity(intent)
    }

    fun refresh(s: String) {
        searchText = s
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