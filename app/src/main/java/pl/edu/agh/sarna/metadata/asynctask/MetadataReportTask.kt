package pl.edu.agh.sarna.wifiPasswords

import android.app.ProgressDialog
import android.content.Context
import android.database.Cursor
import android.os.AsyncTask
import android.util.Log
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.model.calls.CallsLogsInfo
import pl.edu.agh.sarna.db.model.contacts.ContactsInfo
import pl.edu.agh.sarna.db.scripts.callLogsAmount
import pl.edu.agh.sarna.db.scripts.contactsAmount
import pl.edu.agh.sarna.db.scripts.mostFrequentContact
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.report.ReportTask
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.toBoolean

class MetadataReportTask(context: Context, response: AsyncResponse, val runID: Long) : ReportTask(context, response) {
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

        list.add(SubtaskStatus(context.getString(R.string.contacts_amount), contactsAmount(context, runID)))
        list.add(SubtaskStatus(context.getString(R.string.most_frequent_contact), mostFrequentContact(context, runID)))

        return list
    }
    private fun generateCallLogsReport(): ArrayList<SubtaskStatus>? {
        val list = ArrayList<SubtaskStatus>()
        list.add(SubtaskStatus(context.getString(R.string.call_logs_amount), callLogsAmount(context, runID)))
        return list
    }

    override fun generateList(cursor: Cursor?, projection: Array<String>) : ArrayList<SubtaskStatus>{
        val list = ArrayList<SubtaskStatus>()
        for (task in projection){
            list.add(SubtaskStatus(
                    task.replace("_", " "),
                    cursor!!.getInt(cursor.getColumnIndex(task)).toBoolean()).toEmoji())
        }
        return list
    }

}
