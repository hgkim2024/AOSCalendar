package com.asusoft.calendar.activity.calendar.dialog.filter

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.dialog.filter.enums.SearchFilterType
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.util.`object`.CalculatorUtil
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.search.spinner.SpinnerItem
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.HashMap
import java.util.concurrent.TimeUnit

class DialogFragmentFilter: DialogFragment() {

    companion object {
        fun newInstance(): DialogFragmentFilter {
            return DialogFragmentFilter()
        }
    }

    private lateinit var adapter: RecyclerViewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = context!!
        val view = inflater.inflate(R.layout.dialog_recyclerview, container, false)

        val title = view.findViewById<TextView>(R.id.title)
        title.text = "검색 필터"
        
        val list = ArrayList<Any>()

        list.add(
            SpinnerItem(
                SearchFilterType.SEARCH.getTitle(),
                0,
                SearchFilterType.SEARCH.getItems()
            )
        )

        list.add(
            SpinnerItem(
                SearchFilterType.PERIOD.getTitle(),
                0,
                SearchFilterType.PERIOD.getItems()
            )
        )

        adapter = RecyclerViewAdapter(this, list)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val confirmBtn = view.findViewById<TextView>(R.id.confirm_button)
        val cancelBtn = view.findViewById<TextView>(R.id.cancel_button)

        confirmBtn.clicks()
                .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val event = HashMapEvent(HashMap())
                    event.map[DialogFragmentFilter.toString()] = DialogFragmentFilter.toString()

                    GlobalBus.post(event)
                    dismiss()
                }

        cancelBtn.clicks()
                .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    dismiss()
                }


        return view
    }

    override fun onResume() {
        super.onResume()

        val windowManager = activity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val params: WindowManager.LayoutParams = dialog?.window?.attributes ?: return

        val maxWidth = CalculatorUtil.dpToPx(330.0F)
        params.width = (size.x * 0.9).toInt()
        if (params.width > maxWidth) {
            params.width = maxWidth
        }

        val maxHeight = CalculatorUtil.dpToPx(60F * 2 + 40 * 2 + 18 * 2)
        params.height = (size.y * 0.9).toInt()
        if (params.height > maxHeight) {
            params.height = maxHeight
        }

        dialog?.window?.attributes = params
    }
}