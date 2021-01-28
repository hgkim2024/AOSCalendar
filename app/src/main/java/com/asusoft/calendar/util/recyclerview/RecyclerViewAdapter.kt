package com.asusoft.calendar.util.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.enums.AddEventType.*
import com.asusoft.calendar.util.extension.addSeparator
import com.asusoft.calendar.util.recyclerview.RecyclerViewType.*
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.datepicker.DatePickerHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.datepicker.DatePickerItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.edittext.EditTextHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.edittext.EditTextItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.startday.StartDayHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.startday.StartDayItem

class RecyclerViewAdapter(private val typeObject: Any, var list: ArrayList<Any>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        fun settingDivider(context: Context, recyclerView: RecyclerView) {
            val dividerItemDecoration = DividerItemDecoration(
                    context,
                    LinearLayoutManager(context).orientation
            )
            dividerItemDecoration.setDrawable(
                    ContextCompat.getDrawable(
                            context,
                            R.drawable.divider
                    )!!
            )
            recyclerView.addItemDecoration(dividerItemDecoration)
        }
    }

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
                    else -> 0
                }
            }
            else -> 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, getType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        return when(type) {
            ADD_EVENT -> {
                when(getType) {
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

                     else -> {
                         val view = inflater.inflate(R.layout.holder_edit_text, parent, false)
                         EditTextHolder(context, view,this)
                     }
                }
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
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}