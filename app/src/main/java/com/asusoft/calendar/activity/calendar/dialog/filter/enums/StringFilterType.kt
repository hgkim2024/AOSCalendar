package com.asusoft.calendar.activity.calendar.dialog.filter.enums

import com.asusoft.calendar.activity.calendar.SideMenuType

enum class StringFilterType(val value: Int) {
    EVENT_NAME(0),
    VISIT_NAME(1),
    MEMO(2);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }

    fun getTitle(): String {
        return when(this) {
            EVENT_NAME -> "이벤트"
            VISIT_NAME -> "초대 받은 사람"
            MEMO -> "메모"
        }
    }

}