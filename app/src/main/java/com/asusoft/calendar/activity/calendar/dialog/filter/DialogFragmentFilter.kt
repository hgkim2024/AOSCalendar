package com.asusoft.calendar.activity.calendar.dialog.filter

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.dialog.filter.enums.SearchFilterType
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.util.objects.CalculatorUtil
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.objects.ThemeUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.holder.search.spinner.SpinnerItem
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.HashMap
import java.util.concurrent.TimeUnit

class DialogFragmentFilter: DialogFragment() {

    companion object {
        fun newInstance(
                searchType: Int,
                periodType: Int
        ): DialogFragmentFilter {
            val f = DialogFragmentFilter()

            val args = Bundle()
            args.putInt("searchType", searchType)
            args.putInt("periodType", periodType)
            f.arguments = args
            return f
        }
    }

    private lateinit var adapter: RecyclerViewAdapter
    private var searchType = 0
    private var periodType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments!!
        searchType = args.getInt("searchType", 0)
        periodType = args.getInt("periodType", 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = context!!
        val view = inflater.inflate(R.layout.dialog_recyclerview, container, false)

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        val rootLayout = view.findViewById<ConstraintLayout>(R.id.root_layout)

        rootLayout.apply {
            measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            clipToOutline= true
        }

        val backgroundLayout = view.findViewById<ConstraintLayout>(R.id.background_layout)
        backgroundLayout.setBackgroundColor(ThemeUtil.instance.background)

        val headerLayout = view.findViewById<ConstraintLayout>(R.id.header_layout)
        headerLayout.setBackgroundColor(ThemeUtil.instance.colorAccent)

        val title = view.findViewById<TextView>(R.id.title)
        title.setTextColor(ThemeUtil.instance.invertFont)
        title.text = "검색 필터"
        
        val list = ArrayList<Any>()

        list.add(
            SpinnerItem(
                SearchFilterType.SEARCH.getTitle(),
                    searchType,
                SearchFilterType.SEARCH.getItems()
            )
        )

        list.add(
            SpinnerItem(
                SearchFilterType.PERIOD.getTitle(),
                    periodType,
                SearchFilterType.PERIOD.getItems()
            )
        )

        adapter = RecyclerViewAdapter(this, list)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val confirmBtn = view.findViewById<TextView>(R.id.confirm_button)

        confirmBtn.setBackgroundColor(ThemeUtil.instance.colorAccent)
        confirmBtn.setTextColor(ThemeUtil.instance.invertFont)

        confirmBtn.clicks()
                .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val event = HashMapEvent(HashMap())
                    event.map[DialogFragmentFilter.toString()] = DialogFragmentFilter.toString()
                    event.map[SearchFilterType.SEARCH.getTitle()] = getSelectedPosition(SearchFilterType.SEARCH)
                    event.map[SearchFilterType.PERIOD.getTitle()] = getSelectedPosition(SearchFilterType.PERIOD)

                    GlobalBus.post(event)
                    dismiss()
                }

        val cancelBtn = view.findViewById<TextView>(R.id.cancel_button)

        cancelBtn.setBackgroundColor(ThemeUtil.instance.background)
        cancelBtn.setTextColor(ThemeUtil.instance.colorAccent)

        cancelBtn.clicks()
                .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    dismiss()
                }

        val topSeparator = view.findViewById<View>(R.id.top_separator)
        topSeparator.setBackgroundColor(ThemeUtil.instance.colorAccent)

        return view
    }

    private fun getSelectedPosition(type: SearchFilterType): Int {
        val title = type.getTitle()

        for (anyItem in adapter.list) {
            val item = anyItem as? SpinnerItem ?: return 0

            if (item.title == title) {
                return item.selectItemPosition
            }
        }

        return 0
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