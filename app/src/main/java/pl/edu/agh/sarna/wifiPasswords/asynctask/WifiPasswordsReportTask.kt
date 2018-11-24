package pl.edu.agh.sarna.wifiPasswords.asynctask

import android.content.Context
import android.database.Cursor
import android.util.Log
import pl.edu.agh.sarna.db.model.wifi.WifiUtils
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.report.ReportEntry
import pl.edu.agh.sarna.report.asynctask.ReportTask
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.isOreo8_1
import pl.edu.agh.sarna.utils.kotlin.toBoolean
import java.lang.ref.WeakReference

class WifiPasswordsReportTask(contextReference: WeakReference<Context>, response: AsyncResponse, val runID: Long) : ReportTask(contextReference, response) {
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

    override fun doInBackground(vararg p0: Void?): List<ReportEntry>? {
        Thread.sleep(1000)
        val usedProjection = if (isOreo8_1()) projection8_1 else projection
        val list = generateTableReport(runID, WifiUtils.WifiUtilsEntry.TABLE_NAME, usedProjection)!!
        if(list.isEmpty()) return skippedMethod()
        val reportList = ArrayList<ReportEntry>()
        list.forEach {
            reportList.add(ReportEntry(""))
        }
        return reportList
    }

    override fun generateList(cursor: Cursor?, projection: Array<String>) : ArrayList<SubtaskStatus>{
        val list = ArrayList<SubtaskStatus>()
        for (task in projection){
            Log.i("TASK", cursor!!.getColumnIndex(task).toString())
            when(task){
                WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_PASSWORD,
                WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_SSID
                    ->
                    list.add(SubtaskStatus(
                        task.replace("_", " "),
                        cursor.getString(cursor.getColumnIndex(task))))
                else -> list.add(SubtaskStatus(
                        task.replace("_", " "),
                        cursor.getInt(cursor.getColumnIndex(task)).toBoolean()))
            }
        }
        return list
    }

}
