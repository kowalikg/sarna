package pl.edu.agh.sarna.smsToken.task

import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.model.smsToken.SmsPermissions
import pl.edu.agh.sarna.db.model.smsToken.TokenSmsDetails
import pl.edu.agh.sarna.db.scripts.getModeByRunID
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.report.ReportEntry
import pl.edu.agh.sarna.report.asynctask.ReportTask
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.toBoolean
import java.lang.ref.WeakReference


class TokenReportTask(contextReference: WeakReference<Context>, response: AsyncResponse, val runID: Long) : ReportTask(contextReference, response) {
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
        val mode = getModeByRunID(contextReference.get(), runID)
        if (mode !in arrayOf(Mode.TEST.ordinal, Mode.TEST_DUMMY.ordinal)){
            list.addAll(generateTableReport(runID-1, SmsPermissions.SmsPermissionsEntry.TABLE_NAME, projectionPermission, SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_RUN_ID )!!)
            list.addAll(generateTableModeReport(runID-1, TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME, projectionGeneral)!!)
        }
        list.addAll(generateTableReport(runID, SmsPermissions.SmsPermissionsEntry.TABLE_NAME, projectionPermission, SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_RUN_ID )!!)
        list.addAll(generateTableModeReport(runID, TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME, projectionGeneral)!!)
        val reportList = ArrayList<ReportEntry>()
        list.forEach {
            reportList.add(ReportEntry(it.description + ":" + it.value))
        }
        return reportList
    }

    private fun generateTableModeReport(runID: Long, tableName: String, projection: Array<String>): ArrayList<SubtaskStatus>? {
        val db = DbHelper.getInstance(contextReference.get())!!.readableDatabase
        val cursor = db.query(
                tableName,
                projection,
                "${BaseColumns._ID}=?",
                arrayOf("$runID"),
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
            list.add(SubtaskStatus(
                    task.replace("_", " "),
                    cursor!!.getInt(cursor.getColumnIndex(task)).toBoolean()))
        }
        return list
    }

}
