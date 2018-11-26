package pl.edu.agh.sarna.db.scripts

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import android.util.Log
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.model.wifi.WifiPasswords
import pl.edu.agh.sarna.db.model.wifi.WifiUtils
import pl.edu.agh.sarna.utils.kotlin.isOreo8_1
import pl.edu.agh.sarna.utils.kotlin.toInt
import java.util.*

fun insertWifiQuery(context: Context, processID: Long, startTime: Long) : Long? {
    val dbHelper = DbHelper.getInstance(context)
    val db = dbHelper!!.writableDatabase

    val values = ContentValues().apply {
        put(WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_PROCESS_ID, processID)
        put(WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_START_TIME, startTime.toString())
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
fun updateWifiMethod(context: Context, runID: Long, status: Boolean, endTime: Long) {
    val db = DbHelper.getInstance(context)!!.writableDatabase
    val cv = ContentValues()
    cv.put(WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_END_TIME, endTime.toString())
    cv.put(WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_STATUS, status.toInt())

    db.update(WifiPasswords.WifiPasswordsEntry.TABLE_NAME, cv, "${BaseColumns._ID} = ?", arrayOf(runID.toString()));
}