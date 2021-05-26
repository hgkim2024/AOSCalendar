package com.asusoft.calendar.util.objects

import com.asusoft.calendar.realm.RealmEventDay
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import java.util.*

object EventBackupAndRestoreUtil {

    fun backupEvent() {
        val items = RealmEventDay.selectAll()
        val json = Gson().toJson(items)

        Logger.d(json)
    }

    fun restoreEvent() {

    }

}