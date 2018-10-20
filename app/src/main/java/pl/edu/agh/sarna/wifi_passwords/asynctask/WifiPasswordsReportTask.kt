package pl.edu.agh.sarna.wifi_passwords.asynctask

import android.app.ProgressDialog
import android.content.Context
import android.database.Cursor
import android.os.AsyncTask
import android.util.Log
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.model.wifi.WifiUtils
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.isOreo8_1
import pl.edu.agh.sarna.utils.kotlin.toBoolean

class WifiPasswordsReportTask(val context: Context, val response: AsyncResponse, val runID: Long) : AsyncTask<Void, Void, ArrayList<SubtaskStatus>>() {
    private var progDailog = ProgressDialog(context)
    private val projection = arrayOf(
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_STORAGE_PERMISSION_STATUS,
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_CONNECTED_STATUS,
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_PASSWORD_FOUND_STATUS,
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_SSID,
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_PASSWORD
    )
    private val projection8_1 = arrayOf(
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_STORAGE_PERMISSION_STATUS,
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_LOCATION_PERMISSION_STATUS,
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_CONNECTED_STATUS,
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_PASSWORD_FOUND_STATUS,
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_SSID,
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_PASSWORD
    )
    override fun onPreExecute() {
        progDailog.setMessage("Loading...")
        progDailog.isIndeterminate = false
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progDailog.setCancelable(true)
        progDailog.show()
    }

    override fun doInBackground(vararg p0: Void?): ArrayList<SubtaskStatus>? {
        Thread.sleep(1000)
        return getRunDetails()
    }

    override fun onPostExecute(result: ArrayList<SubtaskStatus>?) {
        progDailog.dismiss();
        response.processFinish(result!!)

    }
    private fun getRunDetails() : ArrayList<SubtaskStatus>{
        val db = DbHelper.getInstance(context)!!.readableDatabase
        val usedProjection = if (isOreo8_1()) projection8_1 else projection
        val cursor = db.query(
                WifiUtils.WifiUtilsEntry.TABLE_NAME,
                usedProjection,
                "${WifiUtils.WifiUtilsEntry.COLUMN_NAME_RUN_ID}=?",
                arrayOf("$runID"),
                null, null,
                null
        )
        if (cursor.moveToFirst()) {
            return generateList(cursor, usedProjection)
        }
        return ArrayList()

    }
    private fun generateList(cursor: Cursor?, projection: Array<String>) : ArrayList<SubtaskStatus>{
        val list = ArrayList<SubtaskStatus>()
        for (task in projection){
            Log.i("TASK", cursor!!.getColumnIndex(task).toString())
            when(task){
                WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_PASSWORD,
                WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_SSID
                    ->

                    list.add(SubtaskStatus(
                        task.replace("_", " "),
                        cursor!!.getString(cursor.getColumnIndex(task))))
                else -> list.add(SubtaskStatus(
                        task.replace("_", " "),
                        cursor!!.getInt(cursor.getColumnIndex(task)).toBoolean()).toEmoji())
            }
        }
        return list
    }

}
