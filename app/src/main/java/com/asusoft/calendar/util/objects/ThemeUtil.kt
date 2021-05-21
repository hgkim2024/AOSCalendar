package com.asusoft.calendar.util.objects

import com.asusoft.calendar.realm.RealmTheme
import com.asusoft.calendar.realm.copy.CopyTheme

object ThemeUtil {

    val instance: CopyTheme
        get() {
            var theme = RealmTheme.select()

            if (theme == null) {
                RealmTheme().insert()
                theme = RealmTheme.select()!!
            }

            return theme.getCopy()
        }


}