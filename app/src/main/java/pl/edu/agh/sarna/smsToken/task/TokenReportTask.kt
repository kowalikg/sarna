package pl.edu.agh.sarna.smsToken.task

import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.model.smsToken.SmsPermissions
import pl.edu.agh.sarna.db.model.smsToken.TokenSmsDetails
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.report.ReportEntry
import pl.edu.agh.sarna.report.asynctask.ReportTask
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.toBoolean
import java.lang.ref.WeakReference


class TokenReportTask(contextReference: WeakReference<Context>, response: AsyncResponse, val runID: Long, val mode: Mode) : ReportTask(contextReference, response) {
    private val projectionGeneral = arrayOf(
            TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_STATUS
    )
    private val projectionPermission = arrayOf(
            SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_DEFAULT_APP,
            SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_READ,
            SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_RECEIVE,
            SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_SEND
    )

    override fun doInBackground(vararg p0: Void?): List<ReportEntry>? {
        val list = ArrayList<SubtaskStatus>()
        list.addAll(generateTableReport(runID, SmsPermissions.SmsPermissionsEntry.TABLE_NAME, projectionPermission)!!)
        list.addAll(generateTableModeReport(runID, TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME, projectionGeneral)!!)
        val reportList = ArrayList<ReportEntry>()
        list.forEach {
            reportList.add(ReportEntry(""))
        }
        return reportList
    }

    private fun generateTableModeReport(runID: Long, tableName: String, projection: Array<String>): ArrayList<SubtaskStatus>? {
        val db = DbHelper.getInstance(contextReference.get())!!.readableDatabase
        val cursor = db.query(
                tableName,
                projection,
                "${BaseColumns._ID}=? and ${TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_MODE}=?",
                arrayOf("$runID", "${mode.ordinal}"),
                null, null,
                null
        )
        val list = ArrayList<SubtaskStatus>()
        if (cursor.moveToFirst()) {
            list.addAll(generateList(cursor, projection))
        }
        return list
    }

    override fun generateList(cursor: Cursor?, projection: Array<String>) : ArrayList<SubtaskStatus>{
        val list = ArrayList<SubtaskStatus>()
        for (task in projection){
            when(task){
                TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_MODE
                    ->
                        list.add(SubtaskStatus(
                            task.replace("_", " "),
                            Mode.values()[cursor!!.getInt(cursor.getColumnIndex(task))].description))
                else -> list.add(SubtaskStatus(
                        task.replace("_", " "),
                        cursor!!.getInt(cursor.getColumnIndex(task)).toBoolean()))
            }
        }
        return list
    }

}
