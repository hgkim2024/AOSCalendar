package com.asusoft.calendar.util.recyclerview

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View
import com.asusoft.calendar.application.CalendarApplication
import com.jakewharton.rxbinding4.view.clicks
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

class RecyclerItemClickListener(
        context: Context?,
        recyclerView: RecyclerView,
        private val mListener: OnItemClickListener?
) : OnItemTouchListener {
    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
        fun onItemLongClick(view: View?, position: Int)
    }

    private var doubleClickPreventFlag = true

    private val mGestureDetector: GestureDetector
    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView = view.findChildViewUnder(e.x, e.y)
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            if (doubleClickPreventFlag) {
                doubleClickPreventFlag = false
                mListener.onItemClick(childView, view.getChildAdapterPosition(childView))
                GlobalScope.async {
                    delay(CalendarApplication.THROTTLE)
                    doubleClickPreventFlag = true
                }
            }
        }

        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    init {
        mGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val childView = recyclerView.findChildViewUnder(e.x, e.y)
                if (childView != null && mListener != null) {
                    mListener.onItemLongClick(
                            childView,
                            recyclerView.getChildAdapterPosition(childView)
                    )
                }
            }
        })
    }
}