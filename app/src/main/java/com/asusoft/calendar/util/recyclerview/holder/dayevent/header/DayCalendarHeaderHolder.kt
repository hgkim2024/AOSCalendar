package com.asusoft.calendar.util.recyclerview.holder.dayevent.header

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.start.ActivityStart
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.fragment.day.FragmentDayCalendar
import com.asusoft.calendar.realm.copy.CopyEventDay
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.helper.ItemTouchHelperCallback
import com.asusoft.calendar.util.recyclerview.holder.dayevent.body.DayCalendarBodyItem
import com.jakewharton.rxbinding4.view.clicks
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class DayCalendarHeaderHolder(
        private val typeObject: Any,
        val context: Context,
        val view: View,
        private val adapter: RecyclerViewAdapter
) : RecyclerView.ViewHolder(view) {

    fun bind(position: Int) {

        val item = adapter.list[position] as DayCalendarHeaderItem
        
        val title = view.findViewById<TextView>(R.id.title)
        val headerLayout = view.findViewById<ConstraintLayout>(R.id.header_layout)
        val upDownImageView = view.findViewById<ImageView>(R.id.up_down_icon)
        lateinit var adapter: RecyclerViewAdapter

        title.text = item.date.toStringDay()

        if(item.date == item.date.endOfMonth.startOfDay) {
            if (typeObject is FragmentDayCalendar) {
                if (typeObject.activity is ActivityStart) {
                    (typeObject.activity as ActivityStart).setTitle(item.date.toStringMonth())
                    (typeObject.activity as ActivityStart).setDate(item.date.startOfMonth)
                }
            }
        }

        item.itemList = MonthCalendarUIUtil.getDayEventList(item.date, false)
        val list = getDayCalendarItemList(item.itemList, item.date)
        adapter = RecyclerViewAdapter(this, list)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val itemTouchHelperCallback = ItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(recyclerView)

//        Logger.d("list: ${list.size}")

        if (item.isExpand) {
            upDownImageView.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
        } else {
            adapter.list.clear()
            upDownImageView.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
        }

        headerLayout.clicks()
            .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (item.isExpand) {
                    logItemList(adapter.list)
                    adapter.list.clear()
                    adapter.notifyDataSetChanged()
                    collapseAnimation(recyclerView)
                    upDownImageView.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                } else {
                    item.itemList = MonthCalendarUIUtil.getDayEventList(item.date, false)
                    val list = getDayCalendarItemList(item.itemList, item.date)
                    logItemList(list)
                    adapter.list = list
                    adapter.notifyDataSetChanged()
                    expandAnimation(recyclerView)
                    upDownImageView.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                }

                item.isExpand = !item.isExpand
            }
    }

    private fun addEventItem(list: ArrayList<Any>, date: Date) {
        var isCompleteCount = 0

        for (idx in list.indices) {

            when(val item = list[idx]) {
                is DayCalendarBodyItem -> {
                    when (val event = item.event) {
                        is CopyEventDay -> if (event.isComplete) break
                    }
                }
            }

            isCompleteCount++
        }

        list.add(isCompleteCount, date)
    }

    private fun getDayCalendarItemList(
            itemList: ArrayList<Any>,
            date: Date
    ): ArrayList<Any> {
        val list = ArrayList<Any>()

        for (event in itemList) {
            list.add(DayCalendarBodyItem(
                    date,
                    event
            ))
        }

        addEventItem(list, date)

        return  list
    }

    private fun expandAnimation(view: View) {
        view.measure(0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val targetHeight = view.measuredHeight

        view.layoutParams.height = 0
        view.visibility = View.VISIBLE
        val anim = ValueAnimator.ofInt(view.measuredHeight, targetHeight)
        anim.interpolator = AccelerateInterpolator()
        anim.duration = 300L

        anim.addUpdateListener { animation ->
            val layoutParams = view.layoutParams
            layoutParams.height = (targetHeight * animation.animatedFraction).toInt()
//            Logger.d("expandAnimation update height: ${layoutParams.height}")
            view.layoutParams = layoutParams
        }

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                val layoutParams = view.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        })

        anim.start()
    }

    private fun collapseAnimation(view: View) {
        val targetHeight = 0
        val curHeight = view.height

        view.visibility = View.INVISIBLE
        val anim = ValueAnimator.ofInt(curHeight, targetHeight)
        anim.interpolator = DecelerateInterpolator()
        anim.duration = 300L

        anim.addUpdateListener { animation ->
            val layoutParams = view.layoutParams
            layoutParams.height = (curHeight * (1F - animation.animatedFraction)).toInt()
//            Logger.d("collapseAnimation update height: ${layoutParams.height}")
            view.layoutParams = layoutParams
        }

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                val layoutParams = view.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        })

        anim.start()
    }

    private fun logItemList(list: ArrayList<Any>) {
        for (idx in list.indices) {

            when(val item = list[idx]) {
                is DayCalendarBodyItem -> {
                    when (val event = item.event) {
                        is CopyEventDay -> Logger.d("name: ${event.name}, isComplete: ${event.isComplete}, key: ${event.key}")
                    }
                }
            }

        }
    }
}