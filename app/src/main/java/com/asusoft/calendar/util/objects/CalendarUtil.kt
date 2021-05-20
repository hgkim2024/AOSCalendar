package com.asusoft.calendar.util.objects

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.addEvent.activity.ActivityAddEvent
import com.asusoft.calendar.activity.calendar.fragment.month.FragmentMonthPage
import com.asusoft.calendar.activity.calendar.fragment.month.MonthCalendarUiUtil
import com.asusoft.calendar.activity.calendar.fragment.week.FragmentWeekPage
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.RealmEventDay
import com.asusoft.calendar.realm.copy.CopyEventDay
import com.asusoft.calendar.util.*
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.holiday.LunarCalendar
import com.asusoft.calendar.util.recyclerview.RecyclerItemClickListener
import com.asusoft.calendar.util.recyclerview.RecyclerViewAdapter
import com.asusoft.calendar.util.recyclerview.helper.ItemTouchHelperCallback
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object CalendarUtil {
    const val ANIMATION_DURATION: Long = 150

    private val DAY_HEADER_FONT_SIZE: Float
        get() = PreferenceManager.getFloat(PreferenceKey.DAY_CALENDAR_HEADER_FONT_SIZE, PreferenceKey.DAY_CALENDAR_HEADER_DEFAULT_FONT_SIZE)

    private val DAY_ITEM_FONT_SIZE: Float
        get() = PreferenceManager.getFloat(PreferenceKey.DAY_CALENDAR_ITEM_FONT_SIZE, PreferenceKey.DAY_CALENDAR_ITEM_DEFAULT_FONT_SIZE)

    private val EVENT_HEIGHT
        get() = CalculatorUtil.spToPx(DAY_ITEM_FONT_SIZE) + CalculatorUtil.dpToPx(10.0F)

    fun getDayEventList(date: Date, isHoliday: Boolean = true): ArrayList<Any> {
        val eventList: ArrayList<Any> = ArrayList()
        val eventDayList = RealmEventDay.getOneDayCopyList(date)
        val oneDayCopyList = ArrayList(eventDayList.filter { it.startTime == it.endTime })
        val multiDayCopyList = ArrayList(eventDayList.filter { it.startTime != it.endTime })
        val orderMap = getEventOrderList(date)

        var order = 0

        if (isHoliday) {
            val dateString = String.format("%02d", date.calendarMonth) + String.format("%02d", date.calendarDay)
            val holidayMap = orderMap.filter { it.key <= 1231 }

            if (holidayMap.isNotEmpty()) {
                val holidayList = LunarCalendar.holidayArray("${date.calendarYear}")
                if (holidayMap[dateString.toLong()] != null) {
                    val name = holidayList.first { it.date == dateString }.name
                    eventList.add(name)
                }
            }
        }

        val size = oneDayCopyList.size + multiDayCopyList.size + 5

        while (
            (oneDayCopyList.isNotEmpty()
                    || multiDayCopyList.isNotEmpty())
            || order < size
        ) {
            for (item in orderMap) {
                if (order == item.value) {
                    val dayFilter = oneDayCopyList.filter { it.key == item.key }
                    if (dayFilter.isNotEmpty()) {
                        val filterItem = dayFilter.first()
                        eventList.add(filterItem)
                        oneDayCopyList.remove(filterItem)
                        break
                    }

                    val multiDayFilter = multiDayCopyList.filter { it.key == item.key }
                    if (multiDayFilter.isNotEmpty()) {
                        val filterItem = multiDayFilter.first()
                        eventList.add(filterItem)
                        multiDayCopyList.remove(filterItem)
                        break
                    }
                }
            }
            order++
        }

        return eventList
    }

    fun getEventOrderList(
        weekDate: Date
    ): HashMap<Long, Int> {
        val eventDayList = RealmEventDay.selectOneWeek(weekDate.startOfWeek)
        val multiDayList = eventDayList.filter { it.startTime != it.endTime }
        val oneDayList = eventDayList.filter { it.startTime == it.endTime }

        return getEventOrderList(
            weekDate,
            multiDayList,
            oneDayList
        )
    }


    fun getEventOrderList(
        weekDate: Date,
        realmEventDayList: List<RealmEventDay>,
        realmEventOneDayList: List<RealmEventDay>,
        eventMaxCount: Int = 5
    ): HashMap<Long, Int> {

        val startDateString = String.format("%02d", weekDate.startOfWeek.calendarMonth) + String.format("%02d", weekDate.startOfWeek.calendarDay)
        val endDateString = String.format("%02d", weekDate.endOfWeek.calendarMonth) + String.format("%02d", weekDate.endOfWeek.calendarDay)

        val holidayList = if (weekDate.startOfWeek.calendarMonth == 12 && weekDate.endOfWeek.calendarMonth == 1) {
            LunarCalendar.holidayArray("${weekDate.calendarYear}").filter { it.date <= endDateString }
        } else {
            LunarCalendar.holidayArray("${weekDate.calendarYear}").filter { it.date in startDateString..endDateString }
        }

        val orderMap = HashMap<Long, Int>()
        val dayCheckList = ArrayList<Array<Boolean>>()
        dayCheckList.add(arrayOf(false, false, false, false, false, false, false))

        if (holidayList.isNotEmpty()) {
            for (item in holidayList) {
                val sdf = SimpleDateFormat("yyyyMMdd")
                val cal = Calendar.getInstance()
                cal.time = sdf.parse(item.year + item.date)
                val weekOfDay = cal.time.weekOfDay

//                Logger.d("holiday: ${item.date}, ${item.name}, weekDay: ${weekDate.toStringDay()}")

                orderMap[item.date.toLong()] = 0
                dayCheckList[0][weekOfDay] = true
            }
        }

        orderMultiDay(
            weekDate,
            realmEventDayList.filter { !it.isComplete },
            dayCheckList,
            orderMap
        )

        orderOneDay(
            realmEventOneDayList.filter { !it.isComplete },
            dayCheckList,
            orderMap
        )

        orderMultiDay(
            weekDate,
            realmEventDayList.filter { it.isComplete },
            dayCheckList,
            orderMap
        )

        orderOneDay(
            realmEventOneDayList.filter { it.isComplete },
            dayCheckList,
            orderMap
        )

        if (dayCheckList.size > eventMaxCount) {
            for (idx in dayCheckList.indices) {
                for (index in dayCheckList[idx].indices) {
                    if (dayCheckList[idx][index]) {
                        orderMap[index.toLong()] = idx + 1
                    }
                }
            }
        }

        return orderMap
    }

    private fun orderMultiDay(
        weekDate: Date,
        realmEventDayList: List<RealmEventDay>,
        dayCheckList: ArrayList<Array<Boolean>>,
        orderMap: HashMap<Long, Int>
    ) {
        for (eventMultiDay in realmEventDayList) {
            val startOfWeek = if (eventMultiDay.startTime < weekDate.startOfWeek.time) {
                weekDate.startOfWeek.weekOfDay
            } else {
                Date(eventMultiDay.startTime).weekOfDay
            }

            val endOfWeek = if (eventMultiDay.endTime < weekDate.endOfWeek.time) {
                Date(eventMultiDay.endTime).weekOfDay
            } else {
                weekDate.endOfWeek.weekOfDay
            }

            var index = 0
            loop@ while(true) {
                if (startOfWeek > endOfWeek) break@loop

                if (dayCheckList.size <= index) {
                    dayCheckList.add(arrayOf(false, false, false, false, false, false, false))
                }

                var breakFlag = true

                for (i in startOfWeek..endOfWeek) {
                    if (dayCheckList[index][i]) {
                        breakFlag = false
                        break
                    }
                }

                if (breakFlag) {
                    orderMap[eventMultiDay.key] = index
                    for (i in startOfWeek..endOfWeek) {
                        dayCheckList[index][i] = true
                    }

                    break@loop
                }

                index++
            }
        }
    }

    private fun orderOneDay(
        realmEventOneDayList: List<RealmEventDay>,
        dayCheckList: ArrayList<Array<Boolean>>,
        orderMap: HashMap<Long, Int>
    ) {
        for (eventOneDay in realmEventOneDayList) {
//            Logger.d("event name: ${eventOneDay.getCopy().name}")
            val weekOfDay = Date(eventOneDay.startTime).weekOfDay

            var index = 0
            loop@ while(true) {
                if (dayCheckList.size <= index) {
                    dayCheckList.add(arrayOf(false, false, false, false, false, false, false))
                }

                if (!dayCheckList[index][weekOfDay]) {
                    orderMap[eventOneDay.key] = index
                    dayCheckList[index][weekOfDay] = true

                    break@loop
                }

                index++
            }
        }
    }

    fun setCornerRadiusDrawable(v: View, backgroundColor: Int, r: Float = 2.0F) {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        val r = CalculatorUtil.dpToPx(r).toFloat()
        shape.cornerRadii = floatArrayOf(r, r, r, r, r, r, r, r)
        shape.setColor(backgroundColor)
//        shape.setStroke(3, borderColor)
        v.background = shape
    }

    fun setLeftCornerRadiusDrawable(v: View, backgroundColor: Int) {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        val r = CalculatorUtil.dpToPx(2.0F).toFloat()
        shape.cornerRadii = floatArrayOf(r, r, 0F, 0F, 0F, 0F, r, r)
        shape.setColor(backgroundColor)
        v.background = shape
    }

    fun calendarRefresh(isRemoveDayEventView: Boolean = false) {
        val event = HashMapEvent(java.util.HashMap())
        event.map[ActivityAddEvent.toStringActivity()] = ActivityAddEvent.toStringActivity()

        if (isRemoveDayEventView) {
            event.map["removeDayEventView"] = "removeDayEventView"
        }

        GlobalBus.post(event)
    }

    fun getOneDayEventLayout(
            fragment: Fragment,
            context: Context,
            calendar: ViewGroup,
            eventList: ArrayList<Any>,
            date: Date
    ): ConstraintLayout {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.view_one_day_pop_up, null, false)
        val eventLayout = view.findViewById<ConstraintLayout>(R.id.root_layout)
        val title = view.findViewById<TextView>(R.id.title)
        val emptyTitle = view.findViewById<TextView>(R.id.tv_empty)
        val addButton = view.findViewById<ImageButton>(R.id.add_button)

        calendar.addView(eventLayout)

        title.text = "${eventList.size}개 이벤트"
        title.textSize = DAY_HEADER_FONT_SIZE
        title.setSingleLine()
        title.ellipsize = TextUtils.TruncateAt.MARQUEE

        addButton.clicks()
                .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    selectedDayDate(fragment, context, date)
                }

        if (eventList.isEmpty()) {
            emptyTitle.visibility = View.VISIBLE
            emptyTitle.textSize = DAY_HEADER_FONT_SIZE - 1.0F
            emptyTitle.isClickable = true
            emptyTitle.setSingleLine()
            emptyTitle.ellipsize = TextUtils.TruncateAt.MARQUEE

            emptyTitle.clicks()
                    .throttleFirst(CalendarApplication.THROTTLE, TimeUnit.MILLISECONDS)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        selectedDayDate(fragment, context, date)
                    }
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = RecyclerViewAdapter(fragment, eventList)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        val itemTouchHelperCallback = ItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)
        touchHelper.attachToRecyclerView(recyclerView)

        recyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                        context,
                        recyclerView,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View?, position: Int) {
                                GlobalScope.async(Dispatchers.Main) {
                                    delay(RecyclerViewAdapter.CLICK_DELAY)
                                    val item = adapter.list[position] as? CopyEventDay
                                    if (item != null) {
                                        val event = RealmEventDay.select(item.key)
                                        if (event != null) {
                                            val intent = Intent(context, ActivityAddEvent::class.java)
                                            intent.putExtra("key", item.key)
                                            fragment.startActivity(intent)
//                                            Logger.d("week date: ${date.toStringDay()}, address: $this")
                                        }
                                    }
                                }
                            }

                            override fun onItemLongClick(view: View?, position: Int) {}
                        }
                )
        )

        return eventLayout
    }

    private fun selectedDayDate(
            fragment: Fragment,
            context: Context,
            date: Date
    ) {
        val intent = Intent(context, ActivityAddEvent::class.java)
        intent.putExtra("startDate", date.time)
        intent.putExtra("endDate", date.time)
        fragment.startActivity(intent)
    }

    fun getEdgeEventView(
            context: Context
    ): ConstraintLayout {
        val eventLayout = ConstraintLayout(context)
        val edgeView = View(context)
        val textView = TextView(context)
        val checkBox = CheckBox(context)

        eventLayout.id = View.generateViewId()
        edgeView.id = View.generateViewId()
        textView.id = View.generateViewId()
        checkBox.id = View.generateViewId()

        edgeView.tag = 0
        textView.tag = 1
        checkBox.tag = 2

        eventLayout.addView(edgeView)
        eventLayout.addView(textView)
        eventLayout.addView(checkBox)

        eventLayout.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                EVENT_HEIGHT
        )

        edgeView.layoutParams = ConstraintLayout.LayoutParams(
                CalculatorUtil.dpToPx(4.0F),
                0
        )

        val startPadding = CalculatorUtil.dpToPx(3.0F)
        textView.setPadding(startPadding, 0, startPadding, 0)
        textView.textSize = DAY_ITEM_FONT_SIZE
        textView.setSingleLine()
        textView.ellipsize = TextUtils.TruncateAt.END

        textView.layoutParams = ConstraintLayout.LayoutParams(
                0,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        textView.setTextColor(CalendarApplication.getColor(R.color.font))
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.maxLines = 1

        checkBox.layoutParams = ConstraintLayout.LayoutParams(
                CalculatorUtil.dpToPx(35.0F),
                ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        val set = ConstraintSet()
        set.clone(eventLayout)

        val topMargin = CalculatorUtil.dpToPx(2.0F)
        val startMargin = CalculatorUtil.dpToPx(7.0F)

        set.connect(edgeView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, topMargin)
        set.connect(edgeView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, topMargin)
        set.connect(edgeView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, startMargin)

        set.connect(textView.id, ConstraintSet.START, edgeView.id, ConstraintSet.END)
        set.connect(textView.id, ConstraintSet.END, checkBox.id, ConstraintSet.START)

        set.connect(checkBox.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        set.applyTo(eventLayout)

        return eventLayout
    }


    fun showOneDayEventLayoutAnimation(
            eventLayout: ConstraintLayout,
            bottomFlag: Boolean,
            dialogHeight: Int
    ) {
        val animationSet = AnimationSet(false)

        val scaleAnim = ScaleAnimation(1F, 1F, 0F, 1F)
        animationSet.addAnimation(scaleAnim)

        val translateAnim = TranslateAnimation(0F, 0F, if (bottomFlag) dialogHeight.toFloat() else 0F, 0F)
        animationSet.addAnimation(translateAnim)

        animationSet.duration = ANIMATION_DURATION
        eventLayout.startAnimation(animationSet)
    }

    fun getHideOneDayEventLayoutAnimationSet(
            bottomFlag: Boolean,
            dialogHeight: Int
    ): AnimationSet {
        val animationSet = AnimationSet(false)

        val scaleAnim = ScaleAnimation(1F, 1F, 1F, 0F)
        animationSet.addAnimation(scaleAnim)

        val translateAnim = TranslateAnimation(0F, 0F, 0F, if (bottomFlag) dialogHeight.toFloat() else 0F)
        animationSet.addAnimation(translateAnim)

        animationSet.duration = ANIMATION_DURATION

        return animationSet
    }

    fun locationOneDayEventLayout(
            fragment: Fragment,
            calendar: ConstraintLayout,
            eventLayout: ConstraintLayout,
            dayView: View,
            eventList: java.util.ArrayList<Any>,
            point: Point
    ) {
        var dialogWidth: Int = 150
        var dialogHeight = 30 + 14
        var bottomFlag = false

        dialogWidth = CalculatorUtil.dpToPx(dialogWidth.toFloat())
        dialogHeight = CalculatorUtil.dpToPx(dialogHeight.toFloat())

        if (eventList.isEmpty()) dialogHeight += EVENT_HEIGHT
        dialogHeight += (EVENT_HEIGHT * eventList.size)

        if (point.y + dayView.height + CalculatorUtil.dpToPx(1.0F) < calendar.height) {
            if (point.y + dayView.height + dialogHeight >= calendar.height - 10) {
                val height = calendar.height - point.y - dayView.height - 10

                if (point.y - dayView.height > height - 10) {
                    if (point.y - dayView.height - 10 < dialogHeight) {
                        dialogHeight = point.y - dayView.height - 10
                    }
                } else {
                    dialogHeight = height
                }
            }
        } else {
            if (dialogHeight + dayView.height >= calendar.height - 10) {
                dialogHeight = calendar.height - dayView.height - 10
            }
        }

        eventLayout.layoutParams = ConstraintLayout.LayoutParams(
                dialogWidth,
                dialogHeight
        )

        val set = ConstraintSet()
        set.clone(calendar)

        val topMargin =
                if (point.y + (dayView.height * 2) + dialogHeight >= calendar.height) {
                    bottomFlag = true
                    point.y - dialogHeight
                } else {
                    bottomFlag = false
                    point.y + dayView.height
                }

        val startMargin =
                when(fragment) {
                    is FragmentMonthPage -> {
                        fragment.dialogHeight = dialogHeight
                        fragment.bottomFlag = bottomFlag

                        if (point.x + dialogWidth >= calendar.width)
                            point.x + dayView.width - dialogWidth
                        else
                            point.x
                    }

                    is FragmentWeekPage -> {
                        fragment.dialogHeight = dialogHeight
                        fragment.bottomFlag = bottomFlag

                        if (point.x + dialogWidth >= dayView.width)
                            point.x - dialogWidth
                        else
                            point.x
                    }

                    else -> 0
                }


        set.connect(eventLayout.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, topMargin)
        set.connect(eventLayout.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, startMargin)

        set.applyTo(calendar)
    }
}