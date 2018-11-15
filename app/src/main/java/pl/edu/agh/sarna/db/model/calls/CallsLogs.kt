package pl.edu.agh.sarna.db.model.calls

import android.provider.BaseColumns

object CallsLogs {
    object CallsLogsEntry : BaseColumns {
        const val TABLE_NAME = "CallsLogs"
        const val COLUMN_NAME_RUN_ID = "RunID"
        const val COLUMN_NAME_NAME = "Name"
        const val COLUMN_NAME_NUMBER = "Number"
        const val COLUMN_NAME_TYPE = "Type"
        const val COLUMN_NAME_DATE = "Date"
        const val COLUMN_NAME_DURATION = "Duration"
    }
}