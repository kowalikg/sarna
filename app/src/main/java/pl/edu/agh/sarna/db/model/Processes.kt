package pl.edu.agh.sarna.db.model

import android.provider.BaseColumns

object Processes {
    object ProcessEntry : BaseColumns {
        const val TABLE_NAME = "Processes"
        const val COLUMN_NAME_START_TIME = "Start_time"
        const val COLUMN_NAME_END_TIME = "End_time"
        const val COLUMN_NAME_EXTERNAL_SERVER = "External_server"
        const val COLUMN_NAME_ROOT_ALLOWED = "Root_allowed"
        const val COLUMN_NAME_EDUCATIONAL = "Educational"
        const val COLUMN_NAME_REPORT = "Extended_report"
        const val COLUMN_NAME_SYSTEM_VERSION = "System_version"
    }

}