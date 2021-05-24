package com.asusoft.calendar.activity.calendar.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.NumberPicker
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.activity.ActivityCalendar
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.util.objects.CalculatorUtil
import com.asusoft.calendar.util.calendarMonth
import com.asusoft.calendar.util.calendarYear
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.getToday
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DialogFragmentSelectYearMonth: DialogFragment() {

    companion object {
        fun newInstance(
                date: Date? = null
        ): DialogFragmentSelectYearMonth {
            val f = DialogFragmentSelectYearMonth()

            val args = Bundle()
            if (date != null) {
                args.putLong("date", date.time)
            }

            f.arguments = args
            return f
        }
    }

    var date = Date().getToday()
    lateinit var yearPicker: NumberPicker
    lateinit var monthPicker: NumberPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments!!
        val dateTime = args.getLong("date") as Long
        if (dateTime != 0L) {
            date = Date(dateTime)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = context!!

        val view = inflater.inflate(R.layout.dialog_year_month_picker, container, false)

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        val rootLayout = view.findViewById<ConstraintLayout>(R.id.root_layout)

        rootLayout.apply {
            measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            clipToOutline= true
        }

        yearPicker = view.findViewById<NumberPicker>(R.id.year_picker)
        monthPicker = view.findViewById<NumberPicker>(R.id.month_picker)

        yearPicker.minValue = 1960
        yearPicker.maxValue = 2050
        yearPicker.value = date.calendarYear

        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = date.calendarMonth

        val confirmBtn = view.findViewById<TextView>(R.id.confirm_button)
        val cancelBtn = view.findViewById<TextView>(R.id.cancel_button)

        confirmBtn.clicks()
            .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val event = HashMapEvent(HashMap())
                event.map[DialogFragmentSelectYearMonth.toString()] = DialogFragmentSelectYearMonth.toString()

                val year = yearPicker.value.toString()
                val month = String.format("%02d", monthPicker.value)

                val sdf = SimpleDateFormat("yyyyMM")
                val date = sdf.parse(year + month)
                event.map["date"] = date

                GlobalBus.post(event)

                (activity as? ActivityCalendar)?.setMonthDate(date)

                dismiss()
            }

        cancelBtn.clicks()
            .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                dismiss()
            }

        return view
    }

    override fun onResume() {
        super.onResume()

        val windowManager = activity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val params: WindowManager.LayoutParams = dialog?.window?.attributes ?: return

        params.width = CalculatorUtil.dpToPx(300.0F)
        params.height = CalculatorUtil.dpToPx(250.0F)

        dialog?.window?.attributes = params
    }
}