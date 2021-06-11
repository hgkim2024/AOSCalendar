package com.asusoft.calendar.realm.copy

import com.asusoft.calendar.realm.RealmTheme

class CopyTheme(
    var key: Long,
    var name: String,
    var order: Long,
    var colorAccent: Int,
    var holiday: Int,
    var saturday: Int,
    var background: Int,
    var separator: Int,
    var font: Int,
    var lightFont: Int,
    var invertFont: Int,
    var today: Int,
    var eventFontColor: Int
) {
    fun updateOrder(order: Long) {
        this.order = order
        val item = RealmTheme.selectOne(key)
        item?.updateOrder(order)
    }
}
