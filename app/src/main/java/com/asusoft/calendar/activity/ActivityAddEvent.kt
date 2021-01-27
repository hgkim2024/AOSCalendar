package com.asusoft.calendar.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.getToday
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.addeventholder.edittext.EditTextItem
import com.asusoft.calendar.util.recyclerview.addeventholder.startday.StartDayItem
import java.util.*
import kotlin.collections.ArrayList

class ActivityAddEvent : AppCompatActivity() {

    lateinit var adapter: RecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_recycler)

        val list = ArrayList<Any>()
        list.add(
                EditTextItem(
                        "",
                        "제목"
                )
        )

        list.add(
            StartDayItem(
                Date().getToday(),
                "시작 날짜"
            )
        )

        list.add(
            StartDayItem(
                Date().getToday(),
                "종료 날짜"
            )
        )

        adapter = RecyclerViewAdapter(this, list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
//        RecyclerViewAdapter.settingDivider(baseContext, recyclerView)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(baseContext)
    }

}