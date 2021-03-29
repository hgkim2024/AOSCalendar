package com.asusoft.calendar.util.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.asusoft.calendar.R
import com.asusoft.calendar.activity.calendar.SideMenuType
import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.RealmRecentSearchTerms
import com.asusoft.calendar.realm.copy.CopyEventDay
import com.asusoft.calendar.realm.copy.CopyRecentSearchTerms
import com.asusoft.calendar.util.`object`.CalendarUtil.setCornerRadiusDrawable
import com.asusoft.calendar.util.`object`.CalendarUtil.setLeftCornerRadiusDrawable
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.AddEventType.*
import com.asusoft.calendar.util.`object`.MonthCalendarUIUtil
import com.asusoft.calendar.util.eventbus.GlobalBus
import com.asusoft.calendar.util.eventbus.HashMapEvent
import com.asusoft.calendar.util.extension.addBottomSeparator
import com.asusoft.calendar.util.recyclerview.RecyclerViewType.*
import com.asusoft.calendar.util.recyclerview.RecyclerViewType.ADD_EVENT
import com.asusoft.calendar.util.recyclerview.helper.ItemTouchHelperCallback
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.complete.CompleteHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.complete.CompleteItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.delete.DeleteHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.delete.DeleteItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.edittext.EditTextHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.edittext.EditTextItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.memo.MemoHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.memo.MemoItem
import com.asusoft.calendar.util.recyclerview.holder.calendar.eventpopup.OneDayEventHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.startday.StartDayHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.startday.StartDayItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.visite.VisitHolder
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.visite.VisitItem
import com.asusoft.calendar.util.recyclerview.holder.addeventholder.addperson.PersonHolder
import com.asusoft.calendar.util.recyclerview.holder.calendar.dayevent.body.DayCalendarAddEventHolder
import com.asusoft.calendar.util.recyclerview.holder.calendar.dayevent.body.DayCalendarBodyHolder
import com.asusoft.calendar.util.recyclerview.holder.calendar.dayevent.body.DayCalendarBodyItem
import com.asusoft.calendar.util.recyclerview.holder.calendar.dayevent.header.DayCalendarHeaderHolder
import com.asusoft.calendar.util.recyclerview.holder.calendar.dayevent.body.DayCalendarType
import com.asusoft.calendar.util.recyclerview.holder.calendar.dayevent.body.DayCalendarType.*
import com.asusoft.calendar.util.recyclerview.holder.search.eventsearch.EventSearchHolder
import com.asusoft.calendar.util.recyclerview.holder.search.recentsearch.RecentSearchTermsHolder
import com.asusoft.calendar.util.recyclerview.holder.calendar.selectday.SelectDayHolder
import com.asusoft.calendar.util.recyclerview.holder.setting.CalendarSettingType
import com.asusoft.calendar.util.recyclerview.holder.setting.seekbar.SeekBarHolder
import com.asusoft.calendar.util.recyclerview.holder.setting.seekbar.SeekBarItem
import com.asusoft.calendar.util.recyclerview.holder.setting.switch.SwitchHolder
import com.asusoft.calendar.util.recyclerview.holder.setting.switch.SwitchItem
import com.asusoft.calendar.util.recyclerview.holder.sidemenu.CalendarTypeHolder
import com.asusoft.calendar.util.recyclerview.holder.sidemenu.SideMenuTopHolder
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewAdapter(
        private val typeObject: Any,
        var list: ArrayList<Any>
        ): RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperCallback.ItemTouchHelperAdapter {

    companion object {
        // 클릭 이펙트 딜레이
        const val CLICK_DELAY = 150L
    }

    private val type = RecyclerViewType.getType(typeObject)

    override fun getItemViewType(position: Int): Int {
        return getType(position)
    }

    private fun getType(position: Int): Int {
        val item = list[position]

        return when(type) {
            ADD_EVENT -> {
                return when(item) {
                    is EditTextItem -> TITLE.value
                    is StartDayItem -> START_DAY.value
                    is CompleteItem -> COMPLETE.value
                    is DeleteItem -> DELETE.value
                    is VisitItem -> VISIT.value
                    is MemoItem -> MEMO.value
                    else -> 0
                }
            }

            DAY_CALENDAR_BODY -> {
                return when(item) {
                    is Date -> DayCalendarType.ADD_EVENT.value
                    else -> CHECK_BOX.value
                }
            }

            SIDE_MENU -> {
                if (item is SideMenuType) {
                    return if (item == SideMenuType.TOP) {
                        SideMenuType.TOP.value
                    } else {
                        1
                    }
                }

                return 0
            }

            CALENDAR_SETTING -> {
                return when(item) {
                    is SwitchItem -> CalendarSettingType.SWITCH.value
                    is SeekBarItem -> CalendarSettingType.SEEK_BAR.value
                    else -> CalendarSettingType.SWITCH.value
                }
            }

            else -> 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        return when(type) {
            ADD_EVENT -> {
                when(viewType) {
                    TITLE.value -> {
                        val view = inflater.inflate(R.layout.holder_edit_title, parent, false)
                        view.findViewById<ConstraintLayout>(R.id.root_layout).addBottomSeparator(20.0F)
                        EditTextHolder(context, view,this)
                    }

                    START_DAY.value -> {
                        val view = inflater.inflate(R.layout.holder_start_end_day, parent, false)
                        view.findViewById<ConstraintLayout>(R.id.root_layout).addBottomSeparator(20.0F)
                        StartDayHolder(context, view,this)
                    }

                    COMPLETE.value -> {
                        val view = inflater.inflate(R.layout.holder_switch_button, parent, false)
                        view.findViewById<ConstraintLayout>(R.id.root_layout).addBottomSeparator(20.0F)
                        CompleteHolder(context, view, this)
                    }

                    DELETE.value -> {
                        val view = inflater.inflate(R.layout.holder_delete, parent, false)
                        DeleteHolder(context, view,this)
                    }

                    VISIT.value -> {
                        val view = inflater.inflate(R.layout.holder_visite, parent, false)
                        view.findViewById<ConstraintLayout>(R.id.root_layout).addBottomSeparator(20.0F)
                        VisitHolder(context, view,this)
                    }

                    MEMO.value -> {
                        val view = inflater.inflate(R.layout.holder_edit_memo, parent, false)
                        view.findViewById<ConstraintLayout>(R.id.root_layout).addBottomSeparator(20.0F)
                        MemoHolder(context, view,this)
                    }

                    else -> {
                        val view = inflater.inflate(R.layout.holder_edit_title, parent, false)
                        EditTextHolder(context, view,this)
                    }
                }
            }

            ONE_DAY_EVENT -> {
                val view = MonthCalendarUIUtil.getEdgeEventView(context)
                OneDayEventHolder(typeObject, context, view, this)
            }

            SELECT_DAY -> {
                val view = inflater.inflate(R.layout.holder_select_day, parent, false)
                SelectDayHolder(typeObject, context, view, this)
            }

            DAY_CALENDAR_HEADER -> {
                val view = inflater.inflate(R.layout.holder_day_event_header, parent, false)
                DayCalendarHeaderHolder(typeObject, context, view, this)
            }

            DAY_CALENDAR_BODY -> {
                return when(viewType) {
                    DayCalendarType.ADD_EVENT.value -> {
                        val view = inflater.inflate(R.layout.holder_add_event, parent, false)
                        DayCalendarAddEventHolder(context, view, this)
                    }

                    else -> {
                        val view = inflater.inflate(R.layout.holder_day_event, parent, false)
                        DayCalendarBodyHolder(context, view, this)
                    }
                }
            }

            SIDE_MENU -> {
                when(viewType) {
                    SideMenuType.TOP.value -> {
                        val view = inflater.inflate(R.layout.holder_side_top, parent, false)
                        SideMenuTopHolder(context, view, this)
                    }

                    else -> {
                        val view = inflater.inflate(R.layout.holder_side_item, parent, false)
                        CalendarTypeHolder(typeObject, context, view, this)
                    }
                }
            }

            VISIT_PERSON -> {
                val view = inflater.inflate(R.layout.holder_visit_person, parent, false)
                view.findViewById<ConstraintLayout>(R.id.root_layout).addBottomSeparator(20.0F)
                PersonHolder(context, view, this)
            }

            RECENT_SEARCH -> {
                val view = inflater.inflate(R.layout.holder_recent_search_terms, parent, false)
                view.findViewById<ConstraintLayout>(R.id.root_layout).addBottomSeparator(20.0F)
                RecentSearchTermsHolder(context, view, this)
            }

            EVENT_SEARCH_RESULT -> {
                val view = inflater.inflate(R.layout.holder_event_search_result, parent, false)
                val rootLayout = view.findViewById<ConstraintLayout>(R.id.root_layout)
                val edge = view.findViewById<View>(R.id.edge)
                setCornerRadiusDrawable(rootLayout, CalendarApplication.getColor(R.color.background))
                setLeftCornerRadiusDrawable(edge, CalendarApplication.getColor(R.color.colorAccent))
                EventSearchHolder(context, view, this)
            }

            CALENDAR_SETTING -> {
                when(viewType) {
                    CalendarSettingType.SWITCH.value -> {
                        val view = inflater.inflate(R.layout.holder_switch, parent, false)
                        SwitchHolder(context, view, this)
                    }

                    CalendarSettingType.SEEK_BAR.value -> {
                        val view = inflater.inflate(R.layout.holder_seek_bar, parent, false)
                        SeekBarHolder(context, view, this)
                    }

                    else -> {
                        val view = inflater.inflate(R.layout.holder_switch, parent, false)
                        SwitchHolder(context, view, this)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(type) {
            ADD_EVENT -> {
                when(holder) {
                    is EditTextHolder -> holder.bind(position)
                    is StartDayHolder -> holder.bind(position)
                    is CompleteHolder -> holder.bind(position)
                    is DeleteHolder -> holder.bind(position)
                    is VisitHolder -> holder.bind(position)
                    is MemoHolder -> holder.bind(position)
                }
            }

            ONE_DAY_EVENT -> (holder as OneDayEventHolder).bind(position)
            SELECT_DAY -> (holder as SelectDayHolder).bind(position)
            DAY_CALENDAR_HEADER -> (holder as DayCalendarHeaderHolder).bind(position)
            DAY_CALENDAR_BODY -> {
                when(holder) {
                    is DayCalendarAddEventHolder -> holder.bind(position)
                    is DayCalendarBodyHolder -> holder.bind(position)
                }
            }

            SIDE_MENU -> {
                when(holder) {
                    is SideMenuTopHolder -> holder.bind(position)
                    is CalendarTypeHolder -> holder.bind(position)
                }
            }

            VISIT_PERSON -> (holder as PersonHolder).bind(position)
            RECENT_SEARCH -> (holder as RecentSearchTermsHolder).bind(position)
            EVENT_SEARCH_RESULT -> (holder as EventSearchHolder).bind(position)
            CALENDAR_SETTING -> {
                when(holder) {
                    is SwitchHolder -> holder.bind(position)
                    is SeekBarHolder -> holder.bind(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        // TODO: - 정렬 기능 넣을지 생각해보기
//        swapItems(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int) {
        when(type) {
            DAY_CALENDAR_BODY -> {
                when(list[position]) {
                    is DayCalendarBodyItem -> {
                        val item = list.removeAt(position)
                        val dayItem = (item as DayCalendarBodyItem)
                        when(val event = dayItem.event) {
                            is CopyEventDay -> {
                                event.delete()
                                if (event.startTime != event.endTime) {
                                    val event = HashMapEvent(HashMap())
                                    event.map[DayCalendarBodyHolder.toString()] = DayCalendarBodyHolder.toString()
                                    GlobalBus.post(event)
                                } else {
                                    notifyItemRemoved(position)
                                }
                            }
                        }
                    }
                }
            }

            VISIT_PERSON -> {
                list.removeAt(position)
                notifyItemRemoved(position)
            }

            RECENT_SEARCH -> {
                val item = list.removeAt(position)
                notifyItemRemoved(position)

                if (item is CopyRecentSearchTerms) {
                    RealmRecentSearchTerms.select(item.key)?.delete()
                }
            }

            else -> return
        }
    }

    private fun swapItems(positionFrom: Int, positionTo: Int) {
        Collections.swap(list, positionFrom, positionTo)
        notifyItemMoved(positionFrom, positionTo)
    }
}