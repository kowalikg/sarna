package pl.edu.agh.sarna.db

import android.provider.BaseColumns

object DbScripts {
    const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${Process.ProcessEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "${Process.ProcessEntry.COLUMN_NAME_START_TIME} text, " +
                    "${Process.ProcessEntry.COLUMN_NAME_END_TIME} text, " +
                    "${Process.ProcessEntry.COLUMN_NAME_EXTERNAL_SERVER} integer(10) DEFAULT 0, " +
                    "${Process.ProcessEntry.COLUMN_NAME_ROOT_ALLOWED} integer(10) DEFAULT 0, " +
                    "${Process.ProcessEntry.COLUMN_NAME_EDUCATIONAL} integer(10) DEFAULT 0, " +
                    "${Process.ProcessEntry.COLUMN_NAME_REPORT} integer(10) DEFAULT 0, " +
                    "${Process.ProcessEntry.COLUMN_NAME_SYSTEM_VERSION} real(10)) "

    const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${Process.ProcessEntry.TABLE_NAME}"
}