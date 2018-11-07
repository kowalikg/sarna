package pl.edu.agh.sarna.metadata.asynctask

import android.content.Context
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.model.calls.CallsLogsInfo
import pl.edu.agh.sarna.db.model.contacts.ContactsInfo
import pl.edu.agh.sarna.db.scripts.callLogsAmount
import pl.edu.agh.sarna.db.scripts.contactsAmount
import pl.edu.agh.sarna.db.scripts.mostFrequentContact
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.report.asynctask.ReportTask
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
    override fun doInBackground(vararg p0: Void?): ArrayList<SubtaskStatus>? {
        val list = ArrayList<SubtaskStatus>()

        list.addAll(generateTableReport(runID, CallsLogsInfo.CallsLogsInfoEntry.TABLE_NAME, projectionCalls, CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_RUN_ID)!!)
        list.addAll(generateCallLogsReport()!!)

        list.addAll(generateTableReport(runID, ContactsInfo.ContactsInfoEntry.TABLE_NAME, projectionContacts, ContactsInfo.ContactsInfoEntry.COLUMN_NAME_RUN_ID)!!)
        list.addAll(generateContactsReport()!!)

        return list
    }

    private fun generateContactsReport(): ArrayList<SubtaskStatus>? {
        val list = ArrayList<SubtaskStatus>()

        list.add(SubtaskStatus(contextReference.get()!!.getString(R.string.contacts_amount), contactsAmount(contextReference.get(), runID)))
        list.add(SubtaskStatus(contextReference.get()!!.getString(R.string.most_frequent_contact), mostFrequentContact(contextReference.get()!!, runID)))

        return list
    }
    private fun generateCallLogsReport(): ArrayList<SubtaskStatus>? {
        val list = ArrayList<SubtaskStatus>()
        list.add(SubtaskStatus(contextReference.get()!!.getString(R.string.call_logs_amount), callLogsAmount(contextReference.get(), runID)))
        return list
    }

}
