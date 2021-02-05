package com.asusoft.calendar.realm.copy

import io.realm.annotations.PrimaryKey

data class CopyEventOneDay(
        val key: Long,
        var name: String,
        var time: Long = 0
)
