package com.asusoft.calendar.realm

import com.asusoft.calendar.application.CalendarApplication
import com.asusoft.calendar.realm.copy.CopyRecentSearchTerms
import io.realm.Realm
import io.realm.RealmObject
import io.realm.Sort
import io.realm.annotations.PrimaryKey

open class RealmRecentSearchTerms: RealmObject() {

    @PrimaryKey
    var key: Long = System.currentTimeMillis()

    var name: String = ""

    var type: Int = -1

    companion object {
        fun selectCopyAllList(type: Int): List<CopyRecentSearchTerms> {
            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()

            val items = realm.where(RealmRecentSearchTerms::class.java)
                    .equalTo("type", type)
                    .sort("key", Sort.DESCENDING)
                    .findAll()

            realm.commitTransaction()

            val copyList = ArrayList<CopyRecentSearchTerms>()
            for(item in items) {
                copyList.add(
                        CopyRecentSearchTerms(
                                item.key,
                                item.name,
                                item.type
                        )
                )
            }

            return copyList
        }

        fun selectCopyList(type: Int, like: String): List<CopyRecentSearchTerms> {
            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()

            val items = realm.where(RealmRecentSearchTerms::class.java)
                    .equalTo("type", type)
                    .like("name", "${like}*")
                    .sort("key", Sort.DESCENDING)
                    .findAll()

            realm.commitTransaction()

            val copyList = ArrayList<CopyRecentSearchTerms>()
            for (item in items) {
                copyList.add(
                        CopyRecentSearchTerms(
                                item.key,
                                item.name,
                                item.type
                        )
                )
            }

            return copyList
        }

        // TODO: - like 문 필터 추가
        fun select(key: Long): RealmRecentSearchTerms? {
            val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
            realm.beginTransaction()

            val item = realm.where(RealmRecentSearchTerms::class.java)
                    .equalTo("key", key)
                    .findFirst()

            realm.commitTransaction()

            return item
        }
    }

    fun update(
            name: String,
            type: Int,
    ) {
        val realm = Realm.getInstance(CalendarApplication.getRealmConfig())

        realm.beginTransaction()

        if (name != "") {
            this.name = name
        }

        if (0 <= type) {
            this.type = type
        }

        realm.commitTransaction()
        realm.refresh()
    }

    fun insert() {
        val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
        realm.beginTransaction()
        val list = realm.where(RealmRecentSearchTerms::class.java)
                .equalTo("type", type)
                .sort("key")
                .findAll()

        var count = 0
        while (list.size > 20 + count) {
            list.getOrNull(count)?.deleteFromRealm()
            count++
        }

        val items = realm.where(RealmRecentSearchTerms::class.java)
                .equalTo("type", type)
                .equalTo("name", name)
                .findAll()
        items?.deleteAllFromRealm()

        realm.insertOrUpdate(this)
        realm.commitTransaction()
        realm.refresh()
    }

    fun delete() {
        val realm = Realm.getInstance(CalendarApplication.getRealmConfig())
        realm.beginTransaction()
        this.deleteFromRealm()
        realm.commitTransaction()
        realm.refresh()
    }
}