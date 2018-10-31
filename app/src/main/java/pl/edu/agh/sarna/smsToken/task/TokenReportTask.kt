package pl.edu.agh.sarna.smsToken.task

import android.content.Context
import android.database.Cursor
import pl.edu.agh.sarna.db.model.smsToken.SmsPermissions
import pl.edu.agh.sarna.db.model.smsToken.TokenSmsDetails
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.report.ReportTask
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.toBoolean
import java.lang.ref.WeakReference


class TokenReportTask(contextReference: WeakReference<Context>, response: AsyncResponse, val runID: Long) : ReportTask(contextReference, response) {
    private val projectionGeneral = arrayOf(
            TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_MODE,
            TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_STATUS
    )
    private val projectionPermission = arrayOf(
            SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_READ,
            SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_RECEIVE,
            SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_SEND
    )

    override fun doInBackground(vararg p0: Void?): ArrayList<SubtaskStatus> {
        val list = ArrayList<SubtaskStatus>()
        list.addAll(generateTableReport(runID, SmsPermissions.SmsPermissionsEntry.TABLE_NAME, projectionPermission)!!)
        list.addAll(generateTableReport(runID, TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME, projectionGeneral)!!)
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
                        cursor!!.getInt(cursor.getColumnIndex(task)).toBoolean()).toEmoji())
            }

        }
        return list
    }

}
