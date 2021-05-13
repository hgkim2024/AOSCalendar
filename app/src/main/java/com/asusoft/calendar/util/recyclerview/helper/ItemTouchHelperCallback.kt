package com.asusoft.calendar.util.recyclerview.helper

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.recyclerview.holder.calendar.dayevent.body.DayCalendarAddEventHolder
import com.asusoft.calendar.util.recyclerview.holder.calendar.eventpopup.OneDayEventHolder
import com.asusoft.calendar.util.recyclerview.holder.calendar.eventpopup.OneDayHolidayHolder
import com.orhanobut.logger.Logger

class ItemTouchHelperCallback(val adapter: ItemTouchHelperAdapter) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START

//        Logger.d("getMovementFlags: ${makeMovementFlags(dragFlags, swipeFlags)}")

        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(
            viewHolder: RecyclerView.ViewHolder,
            direction: Int
    ) {
        when(viewHolder) {
            is OneDayHolidayHolder,
            is DayCalendarAddEventHolder -> return
        }

        adapter.onItemDismiss(viewHolder.adapterPosition)
    }

    override fun getMoveThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.4F
    }

    override fun onChildDraw(
            canvas: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
    ) {
        when(viewHolder) {
            is OneDayHolidayHolder,
            is DayCalendarAddEventHolder -> return
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

//            if (viewHolder is OneDayEventHolder) {
//                return
//            }

            val viewItem = viewHolder.itemView
            SwipeBackgroundHelper.paintDrawCommandToStart(canvas, viewItem, R.drawable.ic_baseline_delete_24, dX)
        }
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    interface ItemTouchHelperAdapter {

        /**
         * Called when one item is dragged and dropped into a different position
         */
        fun onItemMoved(fromPosition: Int, toPosition: Int)

        /**
         * Called when one item is swiped away
         */
        fun onItemDismiss(position: Int)

    }

}