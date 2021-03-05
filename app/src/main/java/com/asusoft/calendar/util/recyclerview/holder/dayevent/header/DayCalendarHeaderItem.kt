package com.asusoft.calendar.util.recyclerview.holder.dayevent.header

import java.util.*

data class DayCalendarHeaderItem(val date: Date, var itemList: ArrayList<Any>, var isExpand: Boolean = true)