package com.asusoft.calendar.activity.addEvent.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment

class FragmentAddPerson: Fragment() {

    companion object {
        fun newInstance(
                key: Long
        ): FragmentAddPerson {
            val f = FragmentAddPerson()

            val args = Bundle()
            args.putLong("key", key)
            f.arguments = args
            return f
        }
    }

}