package pl.edu.agh.sarna.db.model.calls

import android.provider.BaseColumns

object CallsLogsInfo {
    object CallsLogsInfoEntry : BaseColumns {
        const val TABLE_NAME = "CallsLogsInfo"
        const val COLUMN_NAME_RUN_ID = "RunID"
        const val COLUMN_NAME_LOG_PERMISSION = "Log_Permission"
        const val COLUMN_NAME_FOUND = "Status"
    }
}