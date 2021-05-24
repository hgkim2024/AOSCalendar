package com.asusoft.calendar.util.objects

import com.asusoft.calendar.realm.RealmTheme
import com.asusoft.calendar.realm.copy.CopyTheme

object ThemeUtil {

    val instance: CopyTheme

    init {
        var theme = RealmTheme.selectOne()

        if (theme == null) {
            RealmTheme().insert()
            theme = RealmTheme.selectOne()!!
        }

        instance = theme.getCopy()
    }


}