package com.asusoft.calendar.realm

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration

class MyRealmMigration : RealmMigration {
    override fun hashCode(): Int {
        return 1
    }

    override fun equals(obj: Any?): Boolean {
        return obj is MyRealmMigration
    }

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val schema = realm.schema
        if (oldVersion == 0L) {

            schema["RealmEventMultiDay"]
                    ?.addField("isComplete", Boolean::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj -> obj["isComplete"] = false }

            schema["RealmEventOneDay"]
                    ?.addField("isComplete", Boolean::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj -> obj["isComplete"] = false }
        }
    }
}