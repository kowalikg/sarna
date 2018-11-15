package pl.edu.agh.sarna.metadata.asynctask

import android.content.Context
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

        val description = "Aby uzyskac dostep do logow nalezalo przydzielic dostep do uprawnien logow"
        val status =  generateTableReport(runID, CallsLogsInfo.CallsLogsInfoEntry.TABLE_NAME, projectionCalls, CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_RUN_ID)!!
        Log.i("STATUS", status[0].value.toString())
        val result = if(status[0].value as Boolean) "Zostalo przydzielone" else "Nie zostalo przydzielone"

        metaList.add(ReportEntry(null, description))
        metaList.add(ReportEntry(status[0].toEmoji(), result))
        if (status[0].value as Boolean){
            metaList.add(ReportEntry(generateCallLogsReport()))

            val graphDescription = "Na podstawie analizy logow, udalo sie uzyskac nastepujace informacje.\n"
            val graph = GraphEntry("Kontakty z najwieksza liczba polaczen kazdego typu:", top5amount(contextReference.get()!!, runID))
            metaList.add(ReportEntry(null, graphDescription, graph))
            val graph2 = GraphEntry("Kontakty z ktorymi rozmawiales najdluzej:", top5duration(contextReference.get()!!, runID))
            val graph3 = GraphEntry("Kontakty z ktorymi rozmawiasz wieczorem:", topNight(contextReference.get()!!, runID))
            metaList.add(ReportEntry(null, "", graph2))
            metaList.add(ReportEntry(null, "", graph3))

        }

//        for (task in generateTableReport(runID, CallsLogsInfo.CallsLogsInfoEntry.TABLE_NAME, projectionCalls, CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_RUN_ID)!!){
//            metaList.add(ReportEntry(task, "", GraphEntry("Top duration length", top5duration(contextReference.get()!!, runID))))
//        }
//        metaList.add(ReportEntry(generateCallLogsReport(), "", GraphEntry("Top duration length", top5duration(contextReference.get()!!, runID))))
//
//
//        for (task in generateTableReport(runID, ContactsInfo.ContactsInfoEntry.TABLE_NAME, projectionContacts, ContactsInfo.ContactsInfoEntry.COLUMN_NAME_RUN_ID)!!){
//            metaList.add(ReportEntry(task))
//        }
//        for (task in generateContactsReport()!!){
//            metaList.add(ReportEntry(task))
//        }
        return metaList
    }

    private fun generateContactsReport(): ArrayList<SubtaskStatus>? {
        val list = ArrayList<SubtaskStatus>()

        list.add(SubtaskStatus(contextReference.get()!!.getString(R.string.contacts_amount), contactsAmount(contextReference.get(), runID)))
        list.add(SubtaskStatus(contextReference.get()!!.getString(R.string.most_frequent_contact), mostFrequentContact(contextReference.get()!!, runID)))

        return list
    }
    private fun generateCallLogsReport(): SubtaskStatus {
        return SubtaskStatus(contextReference.get()!!.getString(R.string.call_logs_amount), callLogsAmount(contextReference.get(), runID))

    }

}
