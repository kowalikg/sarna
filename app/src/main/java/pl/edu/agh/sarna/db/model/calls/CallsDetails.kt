package pl.edu.agh.sarna.db.model.calls

import android.provider.BaseColumns

object CallsDetails {
    object CallsDetailsEntry : BaseColumns {
        const val TABLE_NAME = "CallsDetails"
        const val COLUMN_NAME_PROCESS_ID = "ProcessID"
        const val COLUMN_NAME_START_TIME = "Start_time"
        const val COLUMN_NAME_END_TIME = "End_time"
        const val COLUMN_NAME_STATUS = "Status"
    }
}