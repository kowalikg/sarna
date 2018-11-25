package pl.edu.agh.sarna.wifiPasswords.asynctask

import android.content.Context
import android.database.Cursor
import android.os.Build
import android.util.Log
import pl.edu.agh.sarna.db.model.wifi.WifiUtils
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.report.ReportEntry
import pl.edu.agh.sarna.report.asynctask.ReportTask
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.isOreo8_0
import pl.edu.agh.sarna.utils.kotlin.isOreo8_1
import pl.edu.agh.sarna.utils.kotlin.toBoolean
import pl.edu.agh.sarna.wifiPasswords.values.WifiLogsValues
import java.lang.ref.WeakReference

class WifiPasswordsReportTask(contextReference: WeakReference<Context>, response: AsyncResponse, val runID: Long) : ReportTask(contextReference, response) {
    private val projectionPermission = arrayOf(
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_STORAGE_PERMISSION_STATUS,
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_CONNECTED_STATUS,
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_PASSWORD_FOUND_STATUS
    )
    private val projectionContent = arrayOf(
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_SSID,
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_PASSWORD
    )
    private val projection8_1Permission = arrayOf(
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_STORAGE_PERMISSION_STATUS,
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_LOCATION_PERMISSION_STATUS,
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_CONNECTED_STATUS,
            WifiUtils.WifiUtilsEntry.COLUMN_NAME_PASSWORD_FOUND_STATUS
    )

    override fun doInBackground(vararg p0: Void?): List<ReportEntry>? {
        Thread.sleep(1000)
        var usedProjection = if (isOreo8_1()) projection8_1Permission else projectionPermission
        var list = generateTableReport(runID, WifiUtils.WifiUtilsEntry.TABLE_NAME, usedProjection)!!
        if(list.isEmpty()) return skippedMethod()
        val reportList = ArrayList<ReportEntry>()
        reportList.add(ReportEntry("Wyniki metody:"))
        if (isOreo8_0()){
            reportList.add(ReportEntry("Konfiguracja sieci wzięta z pliku ${WifiLogsValues().wifiFileFromOreo}" +
                    ", ponieważ Twój system jest w wersji ${Build.VERSION.RELEASE} >= Oreo 8.0"))
        } else {
            reportList.add(ReportEntry("Konfiguracja sieci wzięta z pliku ${WifiLogsValues().wifiFileToNougat}" +
                    ", ponieważ Twój system jest w wersji ${Build.VERSION.RELEASE} < Oreo 8.0"))
        }
        reportList.add(ReportEntry("Statusy uprawnień:"))
        list.forEach {
            reportList.add(ReportEntry(it.description + " : " + it.value))
        }
        list = generateTableReport(runID, WifiUtils.WifiUtilsEntry.TABLE_NAME, projectionContent)!!
        reportList.add(ReportEntry("Pozyskane dane:"))
        list.forEach {
            reportList.add(ReportEntry(it.description + " : " + it.value))
        }
        reportList.add(ReportEntry("Wyciek konfiguracji sieci WIFI powoduje następujące zagrożenia:\n" +
                " - Twój sąsiad połączy się do Twojej sieci i będzie zabierał Ci transfer\n" +
                " - Albo będzie robił nielegalne rzeczy będąc podłączonym\n" +
                " - Albo będzie ją podsłuchiwał\n" +
                " - Jeżeli będziesz podpięty do sieci firmowej, to ktoś może się włamać do niej i przeprowadzić atak\n" +
                " - Ktoś może uruchomić klona Twojej sieci i Twój telefon połączy się z nim, co pozwoli przechywcić transfer\n" +
                ""))
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
                        cursor.getInt(cursor.getColumnIndex(task)).toBoolean()).toEmoji())
            }
        }
        return list
    }

}
