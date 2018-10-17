package pl.edu.agh.sarna.db.model.contacts

import android.provider.BaseColumns

object Contacts {
    object ContactsEntry : BaseColumns {
        const val TABLE_NAME = "Contacts"
        const val COLUMN_NAME_TRY_ID = "TryID"
        const val COLUMN_NAME_NAME = "Name"
        const val COLUMN_NAME_NUMBER = "Number"
    }
}