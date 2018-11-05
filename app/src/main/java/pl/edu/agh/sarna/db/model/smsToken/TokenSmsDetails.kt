package pl.edu.agh.sarna.db.model.smsToken

import android.provider.BaseColumns

object TokenSmsDetails {
    object TokenSmsDetailsEntry : BaseColumns {
        const val TABLE_NAME = "TokenSms"
        const val COLUMN_NAME_PROCESS_ID = "ProcessID"
        const val COLUMN_NAME_START_TIME = "Start_time"
        const val COLUMN_NAME_END_TIME = "End_time"
        const val COLUMN_NAME_MODE = "Mode"
        const val COLUMN_NAME_STATUS = "Status"
    }
}