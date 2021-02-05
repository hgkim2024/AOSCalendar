package com.asusoft.calendar.realm.copy

import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

data class CopyEventMultiDay(
        val key: Long,
        var name: String,
        var startTime: Long,
        var endTime: Long
)