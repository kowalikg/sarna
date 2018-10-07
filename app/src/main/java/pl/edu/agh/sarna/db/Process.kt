package pl.edu.agh.sarna.db

import android.provider.BaseColumns

object Process {
    object ProcessEntry : BaseColumns {
        const val TABLE_NAME = "Process"
        const val COLUMN_NAME_START_TIME = "StartTime"
        const val COLUMN_NAME_END_TIME = "EndTime"
        const val COLUMN_NAME_EXTERNAL_SERVER = "ExternalServer"
        const val COLUMN_NAME_ROOT_ALLOWED = "RootAllowed"
        const val COLUMN_NAME_EDUCATIONAL = "Educational"
        const val COLUMN_NAME_REPORT = "Report"
        const val COLUMN_NAME_SYSTEM_VERSION = "SystemVersion"
    }

}