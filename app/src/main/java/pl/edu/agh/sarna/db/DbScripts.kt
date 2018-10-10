package pl.edu.agh.sarna.db

import android.content.ContentValues
import android.content.Context
import android.util.Log
import pl.edu.agh.sarna.db.model.Processes
import pl.edu.agh.sarna.db.model.WifiPasswords
import pl.edu.agh.sarna.db.model.WifiUtils
import pl.edu.agh.sarna.utils.kotlin.booleanToInt
import pl.edu.agh.sarna.utils.kotlin.isOreo8_1
import java.util.*

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

    fun insertWifiUtilsQuery(context: Context, runID: Long, storagePermissionGranted : Boolean,
                             locationPermissionGranted : Boolean, connected : Boolean, passwordFound : Boolean) {
        val dbHelper = DbHelper.getInstance(context)
        val db = dbHelper!!.writableDatabase
        val values = ContentValues().apply {
            put(WifiUtils.WifiUtilsEntry.COLUMN_NAME_RUN_ID, runID)

            put(WifiUtils.WifiUtilsEntry.COLUMN_NAME_STORAGE_PERMISSION_STATUS, booleanToInt(storagePermissionGranted))
            if (isOreo8_1())
                put(WifiUtils.WifiUtilsEntry.COLUMN_NAME_LOCATION_PERMISSION_STATUS, booleanToInt(locationPermissionGranted))

            put(WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_CONNECTED_STATUS, booleanToInt(connected))
            put(WifiUtils.WifiUtilsEntry.COLUMN_NAME_PASSWORD_FOUND_STATUS, booleanToInt(passwordFound))
        }

        val tryID = db?.insert(WifiUtils.WifiUtilsEntry.TABLE_NAME, null, values)
        Log.i("ID", "New runutils ID = $tryID")
    }
    fun updateWifiMethod(context: Context, runID: Long, status: Boolean) {
        val db = DbHelper.getInstance(context)
        val cv = ContentValues().apply {
            put(WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_END_TIME, Calendar.getInstance().timeInMillis.toString())
            put(WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_STATUS, booleanToInt(status))
        }
        db!!.writableDatabase.update(WifiPasswords.WifiPasswordsEntry.TABLE_NAME, cv, "_id = ?", arrayOf(runID.toString()));
    }
}