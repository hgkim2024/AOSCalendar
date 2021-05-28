package com.asusoft.calendar.activity.setting.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.setting.activity.ActivitySetting
import com.asusoft.calendar.util.objects.AlertUtil
import com.asusoft.calendar.util.objects.EventBackupAndRestoreUtil
import com.asusoft.calendar.util.objects.PreferenceKey
import com.asusoft.calendar.util.objects.PreferenceManager
import com.asusoft.calendar.util.recyclerview.RecyclerItemClickListener
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter.Companion.CLICK_DELAY
import com.asusoft.calendar.util.recyclerview.holder.search.spinner.SpinnerItem
import com.asusoft.calendar.util.recyclerview.holder.setting.text.TextItem
import com.asusoft.calendar.util.toString_yyyyMMdd_HHmmss
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

class FragmentSetting: Fragment() {

    companion object {
        fun newInstance(): FragmentSetting {
            return FragmentSetting()
        }
    }

    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView

    private val requestWriteFileActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            activityResult.data?.data.also { uri ->
                writeBackupFile(uri)
            }
        }
    }

    private val requestReadFileActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            activityResult.data?.data.also { uri ->
                Logger.d("requestReadFileActivity")
                AlertUtil.alertOkAndCancel(
                    requireContext(),
                    "동일한 이벤트가 있으면 덮어쓰게 됩니다. 복원하시겠습니까?",
                    getString(R.string.ok)
                ) { _, _ ->
                    readRestoreFile(uri)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = this.requireContext()
        val view = inflater.inflate(R.layout.recyclerview, container, false)

        val list = ArrayList<Any>()

        val fontString = "폰트 크기"
        val backupString = "백업 파일 만들기"
        val restoreString = "복원 파일 가져오기"

        list.add(TextItem(fontString, true))

        val orientationList = ArrayList<String>()
        orientationList.addAll(arrayOf("세로 고정", "자동"))
        list.add(
                SpinnerItem(
                        "화면 방향",
                        PreferenceManager.getInt(PreferenceKey.CALENDAR_ORIENTATION, PreferenceKey.CALENDAR_DEFAULT_ORIENTATION),
                        orientationList,
                        PreferenceKey.CALENDAR_ORIENTATION
                )
        )

        list.add(TextItem(backupString, true))
        list.add(TextItem(restoreString, true))

        adapter = RecyclerViewAdapter(this, list)

        recyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        recyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                        context,
                        recyclerView,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View?, position: Int) {
                                GlobalScope.async(Dispatchers.Main) {
                                    delay(CLICK_DELAY)
                                    when(val item = adapter.list[position]) {
                                        is TextItem -> {
                                            when(item.text) {
                                                fontString -> {
                                                    replaceFragment(
                                                            FragmentSettingFontSize.newInstance(),
                                                            FragmentSettingFontSize.toString()
                                                    )
                                                }

                                                backupString -> {
                                                    createBackupFile()
                                                }

                                                restoreString -> {
                                                    openRestoreFile()
                                                }
                                            }
                                        }

                                        else -> {}
                                    }
                                }
                            }

                            override fun onItemLongClick(view: View?, position: Int) {}
                        }
                )
        )

        return view
    }

    fun replaceFragment(instance: Fragment, tag: String) {
        requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(
                        R.id.fragment,
                        instance,
                        tag
                ).addToBackStack(null).commit()
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

    private fun writeBackupFile(uri: Uri?) {
        if (uri == null) {
            Toast.makeText(requireContext(), "백업 실패", Toast.LENGTH_SHORT).show()
            return
        }
        val text = EventBackupAndRestoreUtil.backupEvent()

        GlobalScope.async (Dispatchers.IO) {
            requireActivity().contentResolver.openFileDescriptor(uri, "w").use {
                FileOutputStream(it!!.fileDescriptor).use { outStream ->
                    val data = text.toByteArray(Charsets.UTF_8)
                    outStream.write(data)
                    outStream.close()
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "백업 성공", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openRestoreFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }

        requestReadFileActivity.launch(intent)
    }

    private fun readRestoreFile(uri: Uri?) {
        if (uri == null) return

        val input: InputStream? = requireActivity().contentResolver.openInputStream(uri)
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

            EventBackupAndRestoreUtil.restoreEvent(json, requireContext())
        }
    }
}