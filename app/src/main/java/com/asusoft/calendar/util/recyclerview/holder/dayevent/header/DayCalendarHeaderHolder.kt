package com.asusoft.calendar.util.recyclerview.holder.dayevent.header

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.fragment.month.FragmentMonthPage.Companion.ANIMATION_DURATION
import com.asusoft.calendar.realm.copy.CopyEventMultiDay
import com.asusoft.calendar.realm.copy.CopyEventOneDay
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.helper.ItemTouchHelperCallback
import com.asusoft.calendar.util.recyclerview.holder.dayevent.body.DayCalendarBodyItem
import com.asusoft.calendar.util.toStringDay
import com.orhanobut.logger.Logger
import java.util.*
import kotlin.collections.ArrayList


class DayCalendarHeaderHolder(
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

        headerLayout.setOnClickListener {
            if (item.isExpand) {
//                logItemList(adapter.list)
                collapseAnimation(recyclerView)
                adapter.list.clear()
                adapter.notifyDataSetChanged()
                upDownImageView.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            } else {
                item.itemList = MonthCalendarUIUtil.getDayEventList(item.date, false)
                val list = getDayCalendarItemList(item.itemList, item.date)
//                logItemList(list)
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
                        is CopyEventOneDay -> if (event.isComplete) break
                        is CopyEventMultiDay -> if (event.isComplete) break
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

        // Set initial height to 0 and show the view
        view.layoutParams.height = 0
        view.visibility = View.VISIBLE
        val anim = ValueAnimator.ofInt(view.measuredHeight, targetHeight)
        anim.interpolator = AccelerateInterpolator()
        anim.duration = ANIMATION_DURATION

        anim.addUpdateListener { animation ->
            val layoutParams = view.layoutParams
            layoutParams.height = (targetHeight * animation.animatedFraction).toInt()
            view.layoutParams = layoutParams
        }

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // At the end of animation, set the height to wrap content
                // This fix is for long views that are not shown on screen
                val layoutParams = view.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        })

        anim.start()
    }

    private fun collapseAnimation(view: View) {

        val targetHeight = 0
        val curHeight = view.height

        view.visibility = View.VISIBLE
        val anim = ValueAnimator.ofInt(curHeight, targetHeight)
        anim.interpolator = AccelerateInterpolator()
        anim.duration = ANIMATION_DURATION

        anim.addUpdateListener { animation ->
            val layoutParams = view.layoutParams
            layoutParams.height = (curHeight * (1 - animation.animatedFraction)).toInt()
            view.layoutParams = layoutParams
        }

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // At the end of animation, set the height to wrap content
                // This fix is for long views that are not shown on screen
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
                        is CopyEventOneDay -> Logger.d("name: ${event.name}, isComplete: ${event.isComplete}, key: ${event.key}")
                        is CopyEventMultiDay -> Logger.d("name: ${event.name}, isComplete: ${event.isComplete}, key: ${event.key}")
                    }
                }
            }

        }
    }
}