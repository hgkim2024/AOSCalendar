package com.asusoft.calendar.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.realm.RealmEventMultiDay
import com.asusoft.calendar.realm.RealmEventOneDay
import com.asusoft.calendar.util.endOfDay
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.getToday
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.edittext.EditTextItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.startday.StartDayItem
import com.asusoft.calendar.util.startOfDay
import java.util.*
import kotlin.collections.ArrayList

class ActivityAddEvent : AppCompatActivity() {

    lateinit var adapter: RecyclerViewAdapter

    companion object {
        fun toStringActivity(): String {
            return "ActivityAddEvent"
        }
    }

    // TODO: - newInstance 로 변경 및 date 넘겨 받기
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

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

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(baseContext)

        val cancelBtn = findViewById<Button>(R.id.cancel_btn)
        cancelBtn.setOnClickListener {
            finish()
        }

        val confirmBtn = findViewById<Button>(R.id.confirm_btn)
        confirmBtn.setOnClickListener {
            if ((adapter.list[0] as EditTextItem).context != "")
                addEventRealm()
            else
                Toast.makeText(applicationContext,"제목을 입력해주세요.",
                        Toast.LENGTH_SHORT).show()
        }
    }


    private fun addEventRealm() {
        lateinit var titleItem: EditTextItem
        lateinit var startDayItem: StartDayItem
        lateinit var endDayItem: StartDayItem

        var startDayCount = 0
        for (item in adapter.list) {
            when (item) {
                is StartDayItem -> {
                    if (startDayCount == 0)
                        startDayItem = item
                    else if (startDayCount == 1)
                        endDayItem = item

                    startDayCount++
                }

                is EditTextItem -> titleItem = item
            }
        }

        if (startDayItem.date.startOfDay == endDayItem.date.startOfDay) {
            // 하루 이벤트
            val eventOneDay = RealmEventOneDay()
            // TODO: - Insert 로 변경
            eventOneDay.update(
                titleItem.context,
                startDayItem.date.startOfDay.time
            )
        } else {
            // 이틀 이상 이벤트
            val eventMultiDay = RealmEventMultiDay()
            // TODO: - Insert 로 변경
            eventMultiDay.update(
                titleItem.context,
                startDayItem.date.startOfDay.time,
                endDayItem.date.endOfDay.time
            )
        }

        val event = HashMapEvent(HashMap())
        event.map[toStringActivity()] = toStringActivity()
        GlobalBus.getBus().post(event)

        finish()
    }

}