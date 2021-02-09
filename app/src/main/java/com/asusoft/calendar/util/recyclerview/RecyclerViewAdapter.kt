package com.asusoft.calendar.util.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.enums.AddEventType.*
import com.asusoft.calendar.realm.copy.CopyEventMultiDay
import com.asusoft.calendar.realm.copy.CopyEventOneDay
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.extension.addSeparator
import com.asusoft.calendar.util.recyclerview.RecyclerViewType.*
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.datepicker.DatePickerHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.datepicker.DatePickerItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.delete.DeleteHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.delete.DeleteItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.edittext.EditTextHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.edittext.EditTextItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.event.OneDayEventHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.startday.StartDayHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.startday.StartDayItem

class RecyclerViewAdapter(private val typeObject: Any, var list: ArrayList<Any>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val type = RecyclerViewType.getType(typeObject)

    override fun getItemViewType(position: Int): Int {
        return getType(position)
    }

    private fun getType(position: Int): Int {
        return when(type) {
            ADD_EVENT -> {
                return when(list[position]) {
                    is EditTextItem -> TITLE.value
                    is StartDayItem -> START_DAY.value
                    is DatePickerItem -> DATE_PICKER.value
                    is DeleteItem -> DELETE.value
                    else -> 0
                }
            }

            ONE_DAY_EVENT -> return position
            else -> 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        return when(type) {
            ADD_EVENT -> {
                when(viewType) {
                    TITLE.value -> {
                        val view = inflater.inflate(R.layout.holder_edit_text, parent, false)
                        view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
                        EditTextHolder(context, view,this)
                    }

                    START_DAY.value -> {
                        val view = inflater.inflate(R.layout.holder_start_end_day, parent, false)
                        view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
                        StartDayHolder(context, view,this)
                    }

                    DATE_PICKER.value -> {
                        val view = inflater.inflate(R.layout.holder_date_picker, parent, false)
                        view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
                        DatePickerHolder(context, view,this)
                    }

                    DELETE.value -> {
                        val view = inflater.inflate(R.layout.holder_delete, parent, false)
                        DeleteHolder(context, view,this)
                    }

                     else -> {
                         val view = inflater.inflate(R.layout.holder_edit_text, parent, false)
                         EditTextHolder(context, view,this)
                     }
                }
            }

            ONE_DAY_EVENT -> {
                val name =
                        when (val item = list[viewType]) {
                            is CopyEventOneDay -> item.name
                            is CopyEventMultiDay -> item.name
                            else -> ""
                        }

                val view = MonthCalendarUIUtil.getEventView(context, name, true)
                OneDayEventHolder(context, view, this)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(type) {
            ADD_EVENT -> {
                when(holder) {
                    is EditTextHolder -> holder.bind(position)
                    is StartDayHolder -> holder.bind(position)
                    is DatePickerHolder -> holder.bind(position)
                    is DeleteHolder -> holder.bind(position)
                }
            }

            ONE_DAY_EVENT -> (holder as OneDayEventHolder).bind(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}