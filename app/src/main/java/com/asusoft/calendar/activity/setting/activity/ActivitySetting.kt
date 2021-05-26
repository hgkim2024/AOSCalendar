package com.asusoft.calendar.activity.setting.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.setting.fragment.FragmentSetting
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.removeActionBarShadow
import com.asusoft.calendar.util.extension.setOrientation
import com.asusoft.calendar.util.objects.AlertUtil
import com.asusoft.calendar.util.objects.EventBackupAndRestoreUtil
import com.asusoft.calendar.util.objects.PreferenceManager.context
import com.asusoft.calendar.util.objects.ThemeUtil
import com.asusoft.calendar.util.toString_yyyyMMdd_HHmmss
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

class ActivitySetting : AppCompatActivity() {

    companion object {
        override fun toString(): String {
            return "ActivitySetting"
        }
    }

    private val requestReadFileActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            activityResult.data?.data.also { uri ->
                // TODO: - 현재 alert 뛰울 시 크래시 발생 - 원인 파악 후 수정할 것
                Logger.d("requestReadFileActivity")
                readRestoreFile(uri)
//                AlertUtil.alertOkAndCancel(
//                    context,
//                    "동일한 이벤트의 경우 덮어쓰게 됩니다. 복원하시겠습니까?",
//                    getString(R.string.ok)
//                ) { _, _ ->
//                    readRestoreFile(uri)
//                }
            }
        }
    }

    private val requestWriteFileActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            activityResult.data?.data.also { uri ->
                writeBackupFile(uri)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        setOrientation()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        removeActionBarShadow()

        val title = findViewById<TextView>(R.id.action_bar_title)
        title.setTextColor(ThemeUtil.instance.font)

        val fragmentLayout = findViewById<RelativeLayout>(R.id.fragment)
        fragmentLayout.setBackgroundColor(ThemeUtil.instance.background)

        if (savedInstanceState == null)
            supportFragmentManager
                    .beginTransaction()
                    .add(
                            R.id.fragment,
                            FragmentSetting.newInstance(),
                            FragmentSetting.toString()
                    )
                    .commit()
//        createBackupFile()
//        openRestoreFile()
    }

    override fun onResume() {
        super.onResume()
        setOrientation()
    }

    override fun finish() {
        val event = HashMapEvent(HashMap())
        event.map[ActivitySetting.toString()] = ActivitySetting.toString()
        GlobalBus.post(event)
        super.finish()
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            android.R.id.home -> {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    fun openRestoreFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }

        requestReadFileActivity.launch(intent)
    }

    fun createBackupFile() {
        val fileName = "AsuCalendar_${Date().toString_yyyyMMdd_HHmmss()}"+ ".txt"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }

        requestWriteFileActivity.launch(intent)
    }

    private fun readRestoreFile(uri: Uri?) {
        if (uri == null) return

        val input: InputStream? = contentResolver.openInputStream(uri)
        val r = BufferedReader(InputStreamReader(input))

        var json = ""

        if (input != null) {
            while(true) {
                try {
                    val line = r.readLine()
                    json += line
                    Log.d("Asu", line)
                } catch (e: Exception) {
                    break
                }
            }

            EventBackupAndRestoreUtil.restoreEvent(json, baseContext)
        }
    }

    private fun writeBackupFile(uri: Uri?) {
        if (uri == null) return
        val text = EventBackupAndRestoreUtil.backupEvent()

        GlobalScope.async (Dispatchers.IO) {
            contentResolver.openFileDescriptor(uri, "w").use {
                FileOutputStream(it!!.fileDescriptor).use { outStream ->
                    val data = text.toByteArray(Charsets.UTF_8)
                    outStream.write(data)
                    outStream.close()
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, "Done writing an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

}