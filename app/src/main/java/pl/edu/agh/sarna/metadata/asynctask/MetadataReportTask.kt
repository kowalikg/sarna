package pl.edu.agh.sarna.metadata.asynctask

import android.content.Context
import android.provider.CallLog
import android.util.Log
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.model.calls.CallsLogsInfo
import pl.edu.agh.sarna.db.model.contacts.ContactsInfo
import pl.edu.agh.sarna.db.scripts.*
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.report.ReportEntry
import pl.edu.agh.sarna.report.asynctask.ReportTask
import pl.edu.agh.sarna.utils.kotlin.GraphEntry
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import java.lang.ref.WeakReference

class MetadataReportTask(contextReference: WeakReference<Context>, response: AsyncResponse, val runID: Long) : ReportTask(contextReference, response) {
    private val projectionCalls = arrayOf(
            CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_LOG_PERMISSION,
            CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_FOUND
    )
    private val projectionContacts = arrayOf(
            ContactsInfo.ContactsInfoEntry.COLUMN_NAME_CONTACTS_PERMISSION,
            ContactsInfo.ContactsInfoEntry.COLUMN_NAME_FOUND
    )
    override fun doInBackground(vararg p0: Void?): List<ReportEntry>? {
        val metaList = ArrayList<ReportEntry>()

        metaList.addAll(generateContactReport())
        metaList.addAll(generateCallReport())
        return metaList

    }

    private fun generateContactReport(): Collection<ReportEntry> {
        val metaList = ArrayList<ReportEntry>()
        val status =  generateTableReport(runID, ContactsInfo.ContactsInfoEntry.TABLE_NAME, projectionContacts, ContactsInfo.ContactsInfoEntry.COLUMN_NAME_RUN_ID)!!
        val result = if(status[0].value as Boolean) contextReference.get()!!.getString(R.string.permission_granted) else contextReference.get()!!.getString(R.string.permission_granted)

        metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.contact_condition)))
        metaList.add(ReportEntry(result))
        if (status[0].value as Boolean){
            val bum = generateContactsReport()
            metaList.add(ReportEntry(bum.description + ":" + bum.value))
            metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.contacts_description)))
        }
        return metaList
    }

    private fun generateCallReport(): Collection<ReportEntry> {
        val metaList = ArrayList<ReportEntry>()
        val status =  generateTableReport(runID, CallsLogsInfo.CallsLogsInfoEntry.TABLE_NAME, projectionCalls, CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_RUN_ID)!!
        val result = if(status[0].value as Boolean) contextReference.get()!!.getString(R.string.permission_granted) else contextReference.get()!!.getString(R.string.permission_granted)

        metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.log_condition)))
        metaList.add(ReportEntry(result))

        if (status[0].value as Boolean){
            val bum = generateCallLogsReport()
            metaList.add(ReportEntry(bum.description + ":" + bum.value))
            metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.log_analyze_title)))

            val graph = GraphEntry(contextReference.get()!!.getString(R.string.most_frequent_contact), top5amount(contextReference.get()!!, runID))
            val graph2 = GraphEntry(contextReference.get()!!.getString(R.string.long_duration_contact), top5duration(contextReference.get()!!, runID))
            val graph3 = GraphEntry(contextReference.get()!!.getString(R.string.night_contact), topNight(contextReference.get()!!, runID))
            val graph4 = GraphEntry(contextReference.get()!!.getString(R.string.frequent_short_contact), top3callFactor(contextReference.get()!!, runID))
            val graph5 = GraphEntry(contextReference.get()!!.getString(R.string.rare_long_contact), top3callFactor(contextReference.get()!!, runID, -1))
            val graph6 = GraphEntry(contextReference.get()!!.getString(R.string.missed_contact), missedCalls(contextReference.get()!!, runID))
            metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.most_frequent_contact_description), graph))
            metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.long_duration_contact_description), graph2))
            metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.night_contact_description), graph3))
            metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.frequent_short_contact_description), graph4))
            metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.rare_long_contact_description), graph5))
            metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.missed_contact_description), graph6))
        }
        return metaList
    }

    private fun generateContactsReport(): SubtaskStatus {
        return SubtaskStatus(contextReference.get()!!.getString(R.string.contacts_amount), contactsAmount(contextReference.get(), runID))

    }
    private fun generateCallLogsReport(): SubtaskStatus {
        return SubtaskStatus(contextReference.get()!!.getString(R.string.call_logs_amount), callLogsAmount(contextReference.get(), runID))

    }

}
