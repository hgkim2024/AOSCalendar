package com.asusoft.calendar.activity.calendar.dialog.filter.enums

import android.content.Context

enum class SearchFilterType(val value: Int) {
    SEARCH(0),
    PERIOD(1);

    fun getTitle(): String {
        return when(this) {
            SEARCH -> "검색 기준"
            PERIOD -> "기간"
        }
    }

    fun getItems(): ArrayList<String> {
        val items = ArrayList<String>()
        return when (this) {
            SEARCH -> {
                for (item in StringFilterType.values()) {
                    items.add(item.getTitle())
                }
                items
            }

            PERIOD -> {
                for (item in DateFilterType.values()) {
                    items.add(item.getTitle())
                }
                items
            }
        }
    }
}