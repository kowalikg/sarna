package pl.edu.agh.sarna.dirtycow.task

import android.content.Context
import android.database.Cursor
import android.util.Log
import pl.edu.agh.sarna.db.model.dirtycow.DirtyCowDetails
import pl.edu.agh.sarna.db.model.dirtycow.DirtyCowInfo
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.report.ReportEntry
import pl.edu.agh.sarna.report.asynctask.ReportTask
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.toBoolean
import java.lang.ref.WeakReference

class DirtyCowReportTask(contextReference: WeakReference<Context>, response: AsyncResponse, private val runID: Long) : ReportTask(contextReference, response) {
    private val projectionInfo = arrayOf(
            DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_BUILD,
            DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_ETA,
            DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_KERNEL,
            DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_VENDOR,
            DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_SELINUX
    )
    private val projectionStatus = arrayOf(
            DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_STATUS
    )
    override fun doInBackground(vararg p0: Void?): List<ReportEntry>? {
        val list = ArrayList<SubtaskStatus>()
        list.addAll(generateTableReport(runID, DirtyCowInfo.DirtyCowInfoEntry.TABLE_NAME, projectionInfo)!!)
        list.addAll(generateTableReport(runID, DirtyCowDetails.DirtyCowDetailsEntry.TABLE_NAME, projectionStatus)!!)
        val reportList = ArrayList<ReportEntry>()
        list.forEach {
            reportList.add(ReportEntry(it.description + " : " + it.value))
        }
        return reportList
    }
    override fun generateList(cursor: Cursor?, projection: Array<String>) : ArrayList<SubtaskStatus>{
        val list = ArrayList<SubtaskStatus>()
        for (task in projection){
            Log.i("TASK", cursor!!.getColumnIndex(task).toString())
            when(task){
                DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_BUILD,
                DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_ETA,
                DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_KERNEL,
                DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_VENDOR
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