package pl.edu.agh.sarna.db.model.contacts

import android.provider.BaseColumns

object ContactsInfo {
    object ContactsInfoEntry : BaseColumns {
        const val TABLE_NAME = "ContactsInfo"
        const val COLUMN_NAME_RUN_ID = "RunID"
        const val COLUMN_NAME_CONTACTS_PERMISSION = "ContactsPermission"
        const val COLUMN_NAME_FOUND = "Status"
    }
}