package pl.edu.agh.sarna.db

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import android.util.Log
import pl.edu.agh.sarna.db.model.Processes
import pl.edu.agh.sarna.db.model.WifiPasswords
import pl.edu.agh.sarna.db.model.WifiUtils
import pl.edu.agh.sarna.db.model.calls.CallsDetails
import pl.edu.agh.sarna.db.model.calls.CallsLogs
import pl.edu.agh.sarna.db.model.calls.CallsLogsInfo
import pl.edu.agh.sarna.metadata.MetadataActivity
import pl.edu.agh.sarna.utils.kotlin.isOreo8_1
import pl.edu.agh.sarna.utils.kotlin.toInt
import java.util.*
import android.database.DatabaseUtils


object DbScripts {
    fun updateProcess(context : Context, processID: Long) {
        val db = DbHelper.getInstance(context)

        val cv = ContentValues()
        cv.put(Processes.ProcessEntry.COLUMN_NAME_END_TIME, Calendar.getInstance().timeInMillis.toString())
        db!!.writableDatabase.update(Processes.ProcessEntry.TABLE_NAME, cv, "_id = ?", arrayOf(processID.toString()));

    }
    fun insertWifiQuery(context: Context, processID: Long) : Long? {
        val dbHelper = DbHelper.getInstance(context)
        val db = dbHelper!!.writableDatabase

        val values = ContentValues().apply {
            put(WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_PROCESS_ID, processID)
            put(WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_START_TIME, Calendar.getInstance().timeInMillis.toString())
        }

        val runID = db?.insert(WifiPasswords.WifiPasswordsEntry.TABLE_NAME, null, values)
        Log.i("ID", "New run ID = $runID")

        return runID
    }

    fun insertWifiUtilsQuery(context: Context, runID: Long, storagePermissionGranted: Boolean,
                             locationPermissionGranted: Boolean, connected: Boolean, passwordFound: Boolean, wifiSSID: String, passwordContent: String) {
        val dbHelper = DbHelper.getInstance(context)
        val db = dbHelper!!.writableDatabase
        val values = ContentValues().apply {
            put(WifiUtils.WifiUtilsEntry.COLUMN_NAME_RUN_ID, runID)

            put(WifiUtils.WifiUtilsEntry.COLUMN_NAME_STORAGE_PERMISSION_STATUS, storagePermissionGranted.toInt())
            if (isOreo8_1())
                put(WifiUtils.WifiUtilsEntry.COLUMN_NAME_LOCATION_PERMISSION_STATUS, locationPermissionGranted.toInt())

            put(WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_CONNECTED_STATUS, connected.toInt())
            put(WifiUtils.WifiUtilsEntry.COLUMN_NAME_PASSWORD_FOUND_STATUS, passwordFound.toInt())

            put(WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_SSID, wifiSSID)
            put(WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_PASSWORD, passwordContent)
        }

        val tryID = db?.insert(WifiUtils.WifiUtilsEntry.TABLE_NAME, null, values)
        Log.i("ID", "New runutils ID = $tryID")
    }
    fun updateWifiMethod(context: Context, runID: Long, status: Boolean) {
        val db = DbHelper.getInstance(context)!!.writableDatabase
        val cv = ContentValues()
        cv.put(WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_END_TIME, Calendar.getInstance().timeInMillis.toString())
        cv.put(WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_STATUS, status.toInt())

        db.update(WifiPasswords.WifiPasswordsEntry.TABLE_NAME, cv, "${BaseColumns._ID} = ?", arrayOf(runID.toString()));
    }

    fun insertCallsQuery(context: Context, processID: Long) : Long? {
        val dbHelper = DbHelper.getInstance(context)
        val db = dbHelper!!.writableDatabase

        val values = ContentValues().apply {
            put(CallsDetails.CallsDetailsEntry.COLUMN_NAME_PROCESS_ID, processID)
            put(CallsDetails.CallsDetailsEntry.COLUMN_NAME_START_TIME, Calendar.getInstance().timeInMillis.toString())
        }

        val runID = db?.insert(CallsDetails.CallsDetailsEntry.TABLE_NAME, null, values)
        Log.i("ID", "New run ID = $runID")

        return runID
    }

    fun updateCallsMethod(metadataActivity: MetadataActivity, runID: Long, status: Boolean) {
        val db = DbHelper.getInstance(metadataActivity)!!.writableDatabase
        val cv = ContentValues()
        cv.put(CallsDetails.CallsDetailsEntry.COLUMN_NAME_END_TIME, Calendar.getInstance().timeInMillis.toString())
        cv.put(CallsDetails.CallsDetailsEntry.COLUMN_NAME_STATUS, status.toInt())

        db.update(CallsDetails.CallsDetailsEntry.TABLE_NAME, cv, "${BaseColumns._ID} = ?", arrayOf(runID.toString()));
    }

    fun insertCallsLogsInfoQuery(context: Context, runID: Long, callLogsPermissionGranted: Boolean): Long {
        val dbHelper = DbHelper.getInstance(context)
        val db = dbHelper!!.writableDatabase

        val values = ContentValues().apply {
            put(CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_RUN_ID, runID)
            put(CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_LOG_PERMISSION, callLogsPermissionGranted)
        }

        val tryID = db?.insert(CallsLogsInfo.CallsLogsInfoEntry.TABLE_NAME, null, values)
        Log.i("ID", "New run ID = $runID")

        return tryID!!
    }

    fun insertCallsLogs(context: Context, tryID: Long, name: String?, number: String?, type: String?, time: String?) : Long {
        val dbHelper = DbHelper.getInstance(context)
        val db = dbHelper!!.writableDatabase

        val values = ContentValues().apply {
            put(CallsLogs.CallsLogsEntry.COLUMN_NAME_TRY_ID, tryID)
            put(CallsLogs.CallsLogsEntry.COLUMN_NAME_NAME, name)
            put(CallsLogs.CallsLogsEntry.COLUMN_NAME_NUMBER, number)
            put(CallsLogs.CallsLogsEntry.COLUMN_NAME_TYPE, type!!.toInt())
            put(CallsLogs.CallsLogsEntry.COLUMN_NAME_TIME, time)
        }

        return db?.insert(CallsLogs.CallsLogsEntry.TABLE_NAME, null, values)!!
    }

    fun updateCallsLogsInfoQuery(context: Activity, runID: Long, status: Boolean) {
        val db = DbHelper.getInstance(context)!!.writableDatabase
        val cv = ContentValues()
        cv.put(CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_FOUND, status.toInt())
        db.update(CallsLogsInfo.CallsLogsInfoEntry.TABLE_NAME, cv, "${BaseColumns._ID} = ?", arrayOf(runID.toString()));
    }

    fun callLogsAmount(context: Activity, tryID: Long): Long {
        val db = DbHelper.getInstance(context)!!.readableDatabase
        return DatabaseUtils.queryNumEntries(db, CallsLogs.CallsLogsEntry.TABLE_NAME,
                "${CallsLogs.CallsLogsEntry.COLUMN_NAME_TRY_ID}=?", arrayOf(tryID.toString()))
    }

}