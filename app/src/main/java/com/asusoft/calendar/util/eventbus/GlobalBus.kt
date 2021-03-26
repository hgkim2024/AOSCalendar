package com.asusoft.calendar.util.eventbus

import org.greenrobot.eventbus.EventBus

class GlobalBus {
    companion object {
        var sBus: EventBus? = null

        fun getBus(): EventBus {
            if (sBus == null) sBus = EventBus.getDefault()
            return sBus!!
        }

        fun register(any: Any) {
            if (!getBus().isRegistered(any)) {
                getBus().register(any)
            }
        }

        fun unregister(any: Any) {
            if (getBus().isRegistered(any)) {
                getBus().unregister(any)
            }
        }

        fun post(event: HashMapEvent) {
            getBus().post(event)
        }
    }
}