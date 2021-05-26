package com.asusoft.calendar.util.objects

import android.content.Context
import android.widget.Toast
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.realm.copy.CopyBackupEventDays
import com.asusoft.calendar.realm.copy.CopyEventDay
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.*


object EventBackupAndRestoreUtil {

    fun backupEvent(): String {
        val items = RealmEventDay.selectAll()
        val json = Gson().toJson(items)

        Logger.d(json)
        return json
    }

    fun restoreEvent(json: String, context: Context) {
        try {
            var convertJson = json

            while(json.substring(convertJson.length -4, convertJson.length) == "null") {
                convertJson = convertJson.substring(0, convertJson.length - 4)
            }

            val gson = Gson()
            val backupEventDays = gson.fromJson(convertJson, CopyBackupEventDays::class.java)

            for (item in backupEventDays.items) {
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