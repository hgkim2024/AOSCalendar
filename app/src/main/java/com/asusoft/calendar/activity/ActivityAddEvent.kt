package com.asusoft.calendar.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.fragment.month.FragmentMonthPage
import com.asusoft.calendar.realm.RealmEventMultiDay
import com.asusoft.calendar.realm.RealmEventOneDay
import com.asusoft.calendar.util.endOfDay
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.delete.DeleteHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.delete.DeleteItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.edittext.EditTextItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.startday.StartDayItem
import com.asusoft.calendar.util.startOfDay
import com.asusoft.calendar.util.toStringDay
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList

class ActivityAddEvent : AppCompatActivity() {

    lateinit var adapter: RecyclerViewAdapter
    var isEdit: Boolean = false
    var key = -1L

    companion object {
        fun toStringActivity(): String {
            return "ActivityAddEvent"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        val startDate = intent.getSerializableExtra("startDate") as Date
        val endDate = intent.getSerializableExtra("endDate") as Date

        // TODO: - 수정 시 아래 변수 추가로 보내줄 것
        val title = intent.getStringExtra("title")
        isEdit = intent.getBooleanExtra("isEdit", false)
        key = intent.getLongExtra("key", -1)

        val list = ArrayList<Any>()
        list.add(
            EditTextItem(
                    title ?: "",
                "제목"
            )
        )

        list.add(
            StartDayItem(
                    startDate,
                "시작 날짜"
            )
        )

        list.add(
            StartDayItem(
                    endDate,
                "종료 날짜"
            )
        )

        if (isEdit) {
            list.add(
                    DeleteItem(key)
            )
        }

        adapter = RecyclerViewAdapter(this, list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)

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

    override fun onStart() {
        super.onStart()
        
        GlobalBus.getBus().register(this)
    }

    override fun onStop() {
        super.onStop()

        GlobalBus.getBus().unregister(this)
    }

    // TODO: - update 와 add 분기
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

        when(isEdit) {
            true -> {
                val oneDayItem = RealmEventOneDay.select(key)
                oneDayItem?.delete()

                val multiDayItem = RealmEventMultiDay.select(key)
                multiDayItem?.delete()
            }
        }

        if (startDayItem.date.startOfDay == endDayItem.date.startOfDay) {
            // 하루 이벤트
            val eventOneDay = RealmEventOneDay()
            eventOneDay.update(
                    titleItem.context,
                    startDayItem.date.startOfDay.time
            )
            eventOneDay.insert()
        } else {
            // 이틀 이상 이벤트
            val eventMultiDay = RealmEventMultiDay()
            eventMultiDay.update(
                    titleItem.context,
                    startDayItem.date.startOfDay.time,
                    endDayItem.date.endOfDay.time
            )
            eventMultiDay.insert()
        }

        finish()
    }

    override fun finish() {
        calendarRefresh()
        super.finish()
    }

    private fun calendarRefresh() {
        val event = HashMapEvent(HashMap())
        event.map[toStringActivity()] = toStringActivity()
        GlobalBus.getBus().post(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {

        val deleteHolder = event.map.getOrDefault(DeleteHolder.toString(), null)
        if (deleteHolder != null) {
            val key = event.map["key"] as Long
            
            val oneDayItem = RealmEventOneDay.select(key)
            if (oneDayItem != null) {
                oneDayItem.delete()
                finish()
                return
            }

            val multiDayItem = RealmEventMultiDay.select(key)
            if (multiDayItem != null) {
                multiDayItem.delete()
                finish()
                return
            }
        }
    }
}