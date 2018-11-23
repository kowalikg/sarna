package pl.edu.agh.sarna.db

import android.provider.BaseColumns
import pl.edu.agh.sarna.db.model.Processes
import pl.edu.agh.sarna.db.model.calls.CallsDetails
import pl.edu.agh.sarna.db.model.calls.CallsLogs
import pl.edu.agh.sarna.db.model.calls.CallsLogsInfo
import pl.edu.agh.sarna.db.model.contacts.Contacts
import pl.edu.agh.sarna.db.model.contacts.ContactsInfo
import pl.edu.agh.sarna.db.model.dirtycow.DirtyCowDetails
import pl.edu.agh.sarna.db.model.dirtycow.DirtyCowInfo
import pl.edu.agh.sarna.db.model.smsToken.Codes
import pl.edu.agh.sarna.db.model.smsToken.SmsPermissions
import pl.edu.agh.sarna.db.model.smsToken.TokenSmsDetails
import pl.edu.agh.sarna.db.model.wifi.WifiPasswords
import pl.edu.agh.sarna.db.model.wifi.WifiUtils

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
                    "${WifiUtils.WifiUtilsEntry.COLUMN_NAME_RUN_ID} integer, " +
                    "${WifiUtils.WifiUtilsEntry.COLUMN_NAME_STORAGE_PERMISSION_STATUS} integer DEFAULT 0, " +
                    "${WifiUtils.WifiUtilsEntry.COLUMN_NAME_LOCATION_PERMISSION_STATUS} integer DEFAULT 0, " +
                    "${WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_CONNECTED_STATUS} integer DEFAULT 0, " +
                    "${WifiUtils.WifiUtilsEntry.COLUMN_NAME_PASSWORD_FOUND_STATUS} integer DEFAULT 0, " +
                    "${WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_SSID} text, " +
                    "${WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_PASSWORD} text, " +
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

    const val CREATE_CALLS_DETAILS =
            "CREATE TABLE ${CallsDetails.CallsDetailsEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "${CallsDetails.CallsDetailsEntry.COLUMN_NAME_PROCESS_ID} integer, " +
                    "${CallsDetails.CallsDetailsEntry.COLUMN_NAME_START_TIME} text, " +
                    "${CallsDetails.CallsDetailsEntry.COLUMN_NAME_END_TIME} text, " +
                    "${CallsDetails.CallsDetailsEntry.COLUMN_NAME_STATUS} integer DEFAULT 0, " +
                    "FOREIGN KEY (${CallsDetails.CallsDetailsEntry.COLUMN_NAME_PROCESS_ID}) " +
                    "REFERENCES ${Processes.ProcessEntry.TABLE_NAME} (${BaseColumns._ID}))"

    const val CREATE_CALLS_LOG_INFO =
            "CREATE TABLE ${CallsLogsInfo.CallsLogsInfoEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "${CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_RUN_ID} integer, " +
                    "${CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_LOG_PERMISSION} integer DEFAULT 0, " +
                    "${CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_FOUND} integer DEFAULT 0, " +
                    "FOREIGN KEY (${CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_RUN_ID}) " +
                    "REFERENCES ${CallsDetails.CallsDetailsEntry.TABLE_NAME} (${BaseColumns._ID}))"

    const val CREATE_CALLS_LOGS =
            "CREATE TABLE ${CallsLogs.CallsLogsEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "${CallsLogs.CallsLogsEntry.COLUMN_NAME_RUN_ID} integer, " +
                    "${CallsLogs.CallsLogsEntry.COLUMN_NAME_NAME} text, " +
                    "${CallsLogs.CallsLogsEntry.COLUMN_NAME_NUMBER} text, " +
                    "${CallsLogs.CallsLogsEntry.COLUMN_NAME_TYPE} integer, " +
                    "${CallsLogs.CallsLogsEntry.COLUMN_NAME_DATE} text, " +
                    "${CallsLogs.CallsLogsEntry.COLUMN_NAME_DURATION} text, " +
                    "FOREIGN KEY (${CallsLogs.CallsLogsEntry.COLUMN_NAME_RUN_ID}) " +
                    "REFERENCES ${CallsDetails.CallsDetailsEntry.TABLE_NAME} (${BaseColumns._ID}))"

    const val CREATE_CONTACTS_INFO =
            "CREATE TABLE ${ContactsInfo.ContactsInfoEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "${ContactsInfo.ContactsInfoEntry.COLUMN_NAME_RUN_ID} integer, " +
                    "${ContactsInfo.ContactsInfoEntry.COLUMN_NAME_CONTACTS_PERMISSION} integer DEFAULT 0, " +
                    "${ContactsInfo.ContactsInfoEntry.COLUMN_NAME_FOUND} integer DEFAULT 0, " +
                    "FOREIGN KEY (${ContactsInfo.ContactsInfoEntry.COLUMN_NAME_RUN_ID}) " +
                    "REFERENCES ${CallsDetails.CallsDetailsEntry.TABLE_NAME} (${BaseColumns._ID}))"

    const val CREATE_CONTACTS =
            "CREATE TABLE ${Contacts.ContactsEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "${Contacts.ContactsEntry.COLUMN_NAME_RUN_ID} integer, " +
                    "${Contacts.ContactsEntry.COLUMN_NAME_NAME} text, " +
                    "${Contacts.ContactsEntry.COLUMN_NAME_NUMBER} text, " +
                    "FOREIGN KEY (${Contacts.ContactsEntry.COLUMN_NAME_RUN_ID}) " +
                    "REFERENCES ${CallsDetails.CallsDetailsEntry.TABLE_NAME} (${BaseColumns._ID}))"

    const val LOG_PERMISSION = "SELECT ${CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_LOG_PERMISSION} FROM ${CallsLogsInfo.CallsLogsInfoEntry.TABLE_NAME} " +
            "WHERE ${CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_RUN_ID} = ?"
    const val CONTACT_PERMISSION = "SELECT ${ContactsInfo.ContactsInfoEntry.COLUMN_NAME_CONTACTS_PERMISSION} FROM ${ContactsInfo.ContactsInfoEntry.TABLE_NAME} " +
            "WHERE ${ContactsInfo.ContactsInfoEntry.COLUMN_NAME_RUN_ID} = ?"
    const val GET_DURATION = "SELECT ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NAME}, ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NUMBER} , SUM(${CallsLogs.CallsLogsEntry.COLUMN_NAME_DURATION}) AS SumDuration FROM ${CallsLogs.CallsLogsEntry.TABLE_NAME} " +
            "WHERE ${CallsLogs.CallsLogsEntry.COLUMN_NAME_RUN_ID} = ? GROUP BY ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NAME}, ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NUMBER} ORDER BY SumDuration DESC"
    const val TOP_LOGS_AMOUNT = "SELECT ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NAME}, ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NUMBER}, COUNT(*) AS Amount FROM ${CallsLogs.CallsLogsEntry.TABLE_NAME} " +
            "WHERE ${CallsLogs.CallsLogsEntry.COLUMN_NAME_RUN_ID} = ? GROUP BY ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NAME}, ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NUMBER} ORDER BY Amount DESC"

    const val MISSED =
            "SELECT ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NAME}, ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NUMBER}, Count(*) as LogCount from ${CallsLogs.CallsLogsEntry.TABLE_NAME} " +
                    "WHERE ${CallsLogs.CallsLogsEntry.COLUMN_NAME_TYPE} = 3" +
                    " and ${CallsLogs.CallsLogsEntry.COLUMN_NAME_RUN_ID} = ?" +
                    " group by ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NAME}, ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NUMBER} " +
                    "order by LogCount desc"

    const val TOP_5_DURATION = "$GET_DURATION "
    const val TOP_5_AMOUNT = "$TOP_LOGS_AMOUNT "


    const val TOP_NIGHT =
            "SELECT ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NAME}, ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NUMBER},  Count(*) as LogCount from ${CallsLogs.CallsLogsEntry.TABLE_NAME}\n" +
                    "WHERE ${CallsLogs.CallsLogsEntry.COLUMN_NAME_DATE} like \"2%\"\n" +
                    " and ${CallsLogs.CallsLogsEntry.COLUMN_NAME_RUN_ID} = ?" +
                    "group by ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NAME}, ${CallsLogs.CallsLogsEntry.COLUMN_NAME_NUMBER} " +
                    "order by LogCount desc"
    const val SQL_DELETE_ENTRIES = "" +
            "DROP TABLE IF EXISTS ${Processes.ProcessEntry.TABLE_NAME}" +
            "DROP TABLE IF EXISTS ${WifiUtils.WifiUtilsEntry.TABLE_NAME}" +
            "DROP TABLE IF EXISTS ${WifiPasswords.WifiPasswordsEntry.TABLE_NAME}"

    const val CREATE_TOKEN_SMS_DETAILS =
            "CREATE TABLE ${TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "${TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_PROCESS_ID} integer, " +
                    "${TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_START_TIME} text, " +
                    "${TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_END_TIME} text, " +
                    "${TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_STATUS} integer DEFAULT 0, " +
                    "${TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_MODE} integer DEFAULT 0, " +
                    "FOREIGN KEY (${TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_PROCESS_ID}) " +
                    "REFERENCES ${Processes.ProcessEntry.TABLE_NAME} (${BaseColumns._ID}))"

    const val CREATE_SMS_PERMISSIONS =
            "CREATE TABLE ${SmsPermissions.SmsPermissionsEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "${SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_RUN_ID} integer, " +
                    "${SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_SEND} integer default 0, " +
                    "${SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_READ} integer default 0, " +
                    "${SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_RECEIVE} integer default 0, " +
                    "${SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_DEFAULT_APP} integer default 0, " +
                    "FOREIGN KEY (${SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_RUN_ID}) " +
                    "REFERENCES ${TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME} (${BaseColumns._ID}))"

    const val CREATE_CODES =
            "CREATE TABLE ${Codes.CodesEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "${Codes.CodesEntry.COLUMN_NAME_RUN_ID} integer, " +
                    "${Codes.CodesEntry.COLUMN_NAME_CODE} text, " +
                    "${Codes.CodesEntry.COLUMN_NAME_NUMBER} text, " +
                    "FOREIGN KEY (${Codes.CodesEntry.COLUMN_NAME_RUN_ID}) " +
                    "REFERENCES ${TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME} (${BaseColumns._ID}))"

    const val CREATE_DIRTYCOW_DETAILS =
            "CREATE TABLE ${DirtyCowDetails.DirtyCowDetailsEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "${DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_PROCESS_ID} integer, " +
                    "${DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_START_TIME} text, " +
                    "${DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_END_TIME} text, " +
                    "${DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_STATUS} integer DEFAULT 0, " +
                    "FOREIGN KEY (${DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_PROCESS_ID}) " +
                    "REFERENCES ${DirtyCowDetails.DirtyCowDetailsEntry.TABLE_NAME} (${BaseColumns._ID}))"

    const val CREATE_DIRTYCOW_INFO =
            "CREATE TABLE ${DirtyCowInfo.DirtyCowInfoEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "${DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_RUN_ID} integer, " +
                    "${DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_ETA} integer, " +
                    "${DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_SELINUX} integer, " +
                    "${DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_KERNEL} text, " +
                    "${DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_BUILD} text, " +
                    "${DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_VENDOR} text, " +
                    "FOREIGN KEY (${DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_RUN_ID}) " +
                    "REFERENCES ${DirtyCowDetails.DirtyCowDetailsEntry.TABLE_NAME} (${BaseColumns._ID}))"

}