package com.asusoft.calendar.activity.calendar.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.activity.ActivityCalendar
import com.asusoft.calendar.realm.RealmRecentSearchTerms
import com.asusoft.calendar.util.enums.RecentSearchTermsType
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.helper.ItemTouchHelperCallback
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FragmentRecentSearchTerms: Fragment() {

    companion object {
        fun newInstance(): FragmentRecentSearchTerms {
            return FragmentRecentSearchTerms()
        }
    }

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: RecyclerViewAdapter
    lateinit var tvEmpty: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = this.context!!
        val view = inflater.inflate(R.layout.recyclerview, container, false)

        val list = RealmRecentSearchTerms.selectCopyAllList(RecentSearchTermsType.EVENT.value)
        adapter = RecyclerViewAdapter(this, list as ArrayList<Any>)

        recyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        tvEmpty = view.findViewById<TextView>(R.id.tv_empty)
        isEmpty()

        val itemTouchHelperCallback = ItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(recyclerView)

        return view
    }

    fun refresh(s: String?) {
        if (s == null) return

        val list = if (s == "") {
            RealmRecentSearchTerms.selectCopyAllList(RecentSearchTermsType.EVENT.value)
        } else {
            RealmRecentSearchTerms.selectCopyList(RecentSearchTermsType.EVENT.value, s)
        }

        adapter.list = list as ArrayList<Any>
        adapter.notifyDataSetChanged()
        isEmpty()
    }

    private fun isEmpty() {
        if (adapter.list.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "최근 검색어 내역이 없습니다."
        } else {
            tvEmpty.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as? ActivityCalendar)?.fragmentRecentSearchTerms = null
    }
}