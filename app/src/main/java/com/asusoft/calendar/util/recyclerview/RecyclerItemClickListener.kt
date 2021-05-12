package com.asusoft.calendar.util.recyclerview

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View
import android.widget.CheckBox
import com.asusoft.calendar.application.CalendarApplication
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

    private var preventDoubleClickFlag = true

    private val mGestureDetector: GestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
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

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView = view.findChildViewUnder(e.x, e.y)
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {

            // TODO: - ONE_DAY_EVENT 일 때만 적용되어 나중에 코드 분할이 필요 - 어떻게 할지 분할이 필요할 때 정하기
            val checkBox = childView.findViewWithTag<CheckBox?>(2)
            if (checkBox != null) {
                if (checkBox.x <= e.x && e.x <= checkBox.x + checkBox.width) {
                    return false
                }
            }

            if (preventDoubleClickFlag) {
                preventDoubleClickFlag = false
                mListener.onItemClick(childView, view.getChildAdapterPosition(childView))
                GlobalScope.async {
                    delay(CalendarApplication.THROTTLE)
                    preventDoubleClickFlag = true
                }
            }
        }

        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

}