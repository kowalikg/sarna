package pl.edu.agh.sarna.cloak_and_dagger.task

import android.content.Context
import android.database.Cursor
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.model.cloak.CloakInfo
import pl.edu.agh.sarna.db.model.cloak.CloakText
import pl.edu.agh.sarna.db.scripts.getTextByRunID
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.report.ReportEntry
import pl.edu.agh.sarna.report.asynctask.ReportTask
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import java.lang.ref.WeakReference

class CloakReportTask(contextReference: WeakReference<Context>,
                      response: AsyncResponse,
                      val runID: Long) : ReportTask(contextReference, response) {
    private val projectionGeneral = arrayOf(
            CloakInfo.CloakInfoEntry.COLUMN_NAME_STATUS
    )
    private val projectionCodes = arrayOf(
            CloakText.CloakTextEntry.COLUMN_NAME_TEXT,
            CloakText.CloakTextEntry.COLUMN_NAME_PACKAGE
    )
    override fun doInBackground(vararg p0: Void?): List<ReportEntry>? {
        val list = ArrayList<SubtaskStatus>()
        list.addAll(generateTableReport(runID, CloakInfo.CloakInfoEntry.TABLE_NAME, projectionGeneral)!!)
        list.addAll(generateText())
        val reportList = ArrayList<ReportEntry>()
        list.forEach {
            reportList.add(ReportEntry(it.description + ":" + it.value))
        }
        if(reportList.isEmpty()) return skippedMethod()
        reportList.add(ReportEntry(contextReference.get()!!.getString(R.string.cloak_report)))
        return reportList
    }

    private fun generateText(): ArrayList<SubtaskStatus> {
        return getTextByRunID(contextReference.get(), runID)
    }

    override fun generateList(cursor: Cursor?, projection: Array<String>) : ArrayList<SubtaskStatus> {
        val list = ArrayList<SubtaskStatus>()
        for (task in projection){
            list.add(SubtaskStatus(
                    task.replace("_", " "),
                    cursor!!.getInt(cursor.getColumnIndex(task))))
        }
        return list
    }
}