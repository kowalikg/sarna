package pl.edu.agh.sarna.db

import android.provider.BaseColumns
import pl.edu.agh.sarna.db.model.Processes
import pl.edu.agh.sarna.db.model.WifiPasswords
import pl.edu.agh.sarna.db.model.WifiUtils

object DbQueries {
    const val CREATE_PROCESS =
            "CREATE TABLE ${Processes.ProcessEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            "${Processes.ProcessEntry.COLUMN_NAME_START_TIME} text, " +
            "${Processes.ProcessEntry.COLUMN_NAME_END_TIME} text, " +
            "${Processes.ProcessEntry.COLUMN_NAME_EXTERNAL_SERVER} integer DEFAULT 0, " +
            "${Processes.ProcessEntry.COLUMN_NAME_ROOT_ALLOWED} integer DEFAULT 0, " +
            "${Processes.ProcessEntry.COLUMN_NAME_EDUCATIONAL} integer DEFAULT 0, " +
            "${Processes.ProcessEntry.COLUMN_NAME_REPORT} integer DEFAULT 0, " +
            "${Processes.ProcessEntry.COLUMN_NAME_SYSTEM_VERSION} real) "

    const val CREATE_WIFI_UTILS =
            "CREATE TABLE ${WifiUtils.WifiUtilsEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "${WifiUtils.WifiUtilsEntry.COLUMN_NAME_RUN_ID} integer, "  +
                    "${WifiUtils.WifiUtilsEntry.COLUMN_NAME_STORAGE_PERMISSION_STATUS} integer DEFAULT 0, " +
                    "${WifiUtils.WifiUtilsEntry.COLUMN_NAME_LOCATION_PERMISSION_STATUS} integer DEFAULT 0, " +
                    "${WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_CONNECTED_STATUS} integer DEFAULT 0, " +
                    "${WifiUtils.WifiUtilsEntry.COLUMN_NAME_PASSWORD_FOUND_STATUS} integer DEFAULT 0, " +
                    "FOREIGN KEY (${WifiUtils.WifiUtilsEntry.COLUMN_NAME_RUN_ID}) " +
                    "REFERENCES ${WifiPasswords.WifiPasswordsEntry.TABLE_NAME} (${BaseColumns._ID}))"

    const val CREATE_WIFI_PASSWORDS =
            "CREATE TABLE ${WifiPasswords.WifiPasswordsEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "${WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_PROCESS_ID} integer, " +
                    "${WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_START_TIME} text, " +
                    "${WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_END_TIME} text, " +
                    "${WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_STATUS} integer DEFAULT 0, " +
                    "FOREIGN KEY (${WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_PROCESS_ID}) " +
                    "REFERENCES ${Processes.ProcessEntry.TABLE_NAME} (${BaseColumns._ID}))"

    const val SQL_DELETE_ENTRIES = "" +
            "DROP TABLE IF EXISTS ${Processes.ProcessEntry.TABLE_NAME}" +
            "DROP TABLE IF EXISTS ${WifiUtils.WifiUtilsEntry.TABLE_NAME}" +
            "DROP TABLE IF EXISTS ${WifiPasswords.WifiPasswordsEntry.TABLE_NAME}"


    const val SQL_CREATE_ENTRIES = CREATE_PROCESS + CREATE_WIFI_PASSWORDS + CREATE_WIFI_UTILS
}