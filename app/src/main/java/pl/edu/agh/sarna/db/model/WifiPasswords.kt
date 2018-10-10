package pl.edu.agh.sarna.db.model

import android.provider.BaseColumns

object WifiPasswords {
    object WifiPasswordsEntry : BaseColumns {
        const val TABLE_NAME = "WifiPasswords"
        const val COLUMN_NAME_PROCESS_ID = "ProcessID"
        const val COLUMN_NAME_START_TIME = "StartTime"
        const val COLUMN_NAME_END_TIME = "EndTime"
        const val COLUMN_NAME_STATUS = "Status"
    }
}