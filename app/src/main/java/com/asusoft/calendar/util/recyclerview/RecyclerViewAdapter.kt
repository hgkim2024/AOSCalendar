package com.asusoft.calendar.util.recyclerview

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.util.extension.addSeparator
import com.asusoft.calendar.util.recyclerview.RecyclerViewType.*
import com.asusoft.calendar.util.recyclerview.addeventholder.datepicker.DatePickerHolder
import com.asusoft.calendar.util.recyclerview.addeventholder.datepicker.DatePickerItem
import com.asusoft.calendar.util.recyclerview.addeventholder.edittext.EditTextHolder
import com.asusoft.calendar.util.recyclerview.addeventholder.edittext.EditTextItem
import com.asusoft.calendar.util.recyclerview.addeventholder.startday.StartDayHolder
import com.asusoft.calendar.util.recyclerview.addeventholder.startday.StartDayItem

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
        return position
    }

    private fun getType(position: Int): Int {
        return when(type) {
            else -> position
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        return when(type) {
            ADD_EVENT -> {
                when(list[position]) {
                    is EditTextItem -> {
                        val view = inflater.inflate(R.layout.holder_edit_text, parent, false)
                        view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
                        Log.d("Asu", "EditTextHolder")
                        EditTextHolder(context, view,this)
                    }

                    is StartDayItem -> {
                        val view = inflater.inflate(R.layout.holder_start_end_day, parent, false)
                        view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
                        Log.d("Asu", "StartDayHolder")
                        StartDayHolder(context, view,this)
                    }

                    is DatePickerItem -> {
                        val view = inflater.inflate(R.layout.holder_date_picker, parent, false)
                        view.findViewById<ConstraintLayout>(R.id.root_layout).addSeparator(20.0F)
                        Log.d("Asu", "DatePickerHolder")
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