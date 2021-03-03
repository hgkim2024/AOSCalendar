package com.asusoft.calendar.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.dialog.DialogFragmentDaySelectCalendar
import com.asusoft.calendar.fragment.month.FragmentMonthPage
import com.asusoft.calendar.realm.RealmEventMultiDay
import com.asusoft.calendar.realm.RealmEventOneDay
import com.asusoft.calendar.util.`object`.AdUtil
import com.asusoft.calendar.util.`object`.AlertUtil
import com.asusoft.calendar.util.endOfDay
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.recyclerview.RecyclerItemClickListener
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.delete.DeleteHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.delete.DeleteItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.edittext.EditTextItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.startday.StartDayItem
import com.asusoft.calendar.util.recyclerview.holder.selectday.SelectDayItem
import com.asusoft.calendar.util.startOfDay
import com.asusoft.calendar.util.toStringDay
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList

class ActivityAddEvent : AppCompatActivity() {

    lateinit var adapter: RecyclerViewAdapter
    lateinit var adView: AdView
    var isEdit: Boolean = false
    var key = -1L
    var confirmFlag = false

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

        recyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                        this,
                        recyclerView,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View?, position: Int) {
                                val item = adapter.list[position]
                                if (item is StartDayItem) {
                                    val selectDayList = ArrayList<StartDayItem>()

                                    for (item in adapter.list) {
                                        if (item is StartDayItem) {
                                            selectDayList.add(item)
                                        }
                                    }

                                    DialogFragmentDaySelectCalendar
                                            .newInstance(
                                                    selectDayList[0].date,
                                                    selectDayList[1].date.startOfDay
                                            )
                                            .show(supportFragmentManager, "DialogFragmentDaySelectCalendar")
                                }
                            }

                            override fun onItemLongClick(view: View?, position: Int) {}
                        }
                )
        )

        val cancelBtn = findViewById<Button>(R.id.cancel_btn)
        cancelBtn.setOnClickListener {
            finish()
        }

        val confirmBtn = findViewById<Button>(R.id.confirm_btn)
        confirmBtn.setOnClickListener {
            if ((adapter.list[0] as EditTextItem).context != "") {
                if (!confirmFlag) {
                    confirmFlag = true
                    addEventRealm()
                }
            }
            else
                Toast.makeText(applicationContext,"제목을 입력해주세요.",
                        Toast.LENGTH_SHORT).show()
        }


        // 광고 추가
        adView = AdView(baseContext)

        adView.adSize = AdSize.BANNER
        adView.adUnitId = AdUtil.adMobAddEventTopBannerId

        val layout = findViewById<ConstraintLayout>(R.id.ad_layout)
        layout.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    override fun onStart() {
        super.onStart()
        
        GlobalBus.getBus().register(this)
    }

    override fun onStop() {
        super.onStop()

        GlobalBus.getBus().unregister(this)
    }

    public override fun onPause() {
        adView.pause()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
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
            // TODO: - isComplete 설정하는 기능 추가
            val eventOneDay = RealmEventOneDay()
            eventOneDay.update(
                    titleItem.context,
                    startDayItem.date.startOfDay.time,
                    false
            )
            eventOneDay.insert()
        } else {
            // 이틀 이상 이벤트
            // TODO: - isComplete 설정하는 기능 추가
            val eventMultiDay = RealmEventMultiDay()
            eventMultiDay.update(
                    titleItem.context,
                    startDayItem.date.startOfDay.time,
                    endDayItem.date.endOfDay.time,
                    false
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

    private fun removeEvent(key: Long) {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: HashMapEvent) {
        val deleteHolder = event.map.getOrDefault(DeleteHolder.toString(), null)
        if (deleteHolder != null) {
            val key = event.map["key"] as Long
            AlertUtil.alertOkAndCancel(
                    this,
                    "삭제하시겠습니까?",
                    getString(R.string.ok)
            ) { _, _ ->
                removeEvent(key)
            }
        }

        val dialogFragmentDaySelectCalendar = event.map.getOrDefault(DialogFragmentDaySelectCalendar.toString(), null)
        if (dialogFragmentDaySelectCalendar != null) {
            val selectedStartDate = event.map["selectedStartDate"] as? Date
            val selectedEndDate = event.map["selectedEndDate"] as? Date

//            Logger.d("selectedStartDate: ${selectedStartDate?.toStringDay()}")
//            Logger.d("selectedEndDate: ${selectedEndDate?.toStringDay()}")

            val selectDayList = ArrayList<StartDayItem>()

            for (item in adapter.list) {
                if (item is StartDayItem) {
                    selectDayList.add(item)
                }
            }

            if (selectedStartDate != null) {
                selectDayList[0].date = selectedStartDate
            }

            if (selectedEndDate != null) {
                selectDayList[1].date = selectedEndDate
            }

            adapter.notifyDataSetChanged()
        }
    }
}