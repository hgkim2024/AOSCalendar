package com.asusoft.calendar.util.objects

import android.content.Context
import android.widget.Toast
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.realm.copy.CopyEventDay
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async


object EventBackupAndRestoreUtil {

    fun backupEvent(): String {
        val items = RealmEventDay.selectAll()
        val json = Gson().toJson(items)

        Logger.d(json)
        return json
    }

    fun restoreEvent(json: String, context: Context) {
        try {
            val convertJson = CalculatorUtil.jsonConverter(json)

            val gson = Gson()
            val itemType = object : TypeToken<List<CopyEventDay>>() {}.type
            val items = gson.fromJson(convertJson, itemType) as List<CopyEventDay>

            for (item in items) {
                item.insert()
            }

            CalendarUtil.calendarRefresh(true)
            GlobalScope.async(Dispatchers.Main) {
                Toast.makeText(context, "복원 성공", Toast.LENGTH_SHORT).show()
            }
        } catch (e: JsonSyntaxException) {
            GlobalScope.async(Dispatchers.Main) {
                Toast.makeText(context, "복원 할 수 없는 파일입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}