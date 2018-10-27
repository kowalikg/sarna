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
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.toBoolean

class MetadataReportTask(val context: Context, val response: AsyncResponse, val runID: Long) : AsyncTask<Void, Void, ArrayList<SubtaskStatus>>() {
    private var progDailog = ProgressDialog(context)

    override fun onPreExecute() {
        progDailog.setMessage("Loading...")
        progDailog.isIndeterminate = false
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progDailog.setCancelable(true)
        progDailog.show()
    }

    override fun doInBackground(vararg p0: Void?): ArrayList<SubtaskStatus>? {
        val list = ArrayList<SubtaskStatus>()

        list.addAll(generateCallsInfoReport(CallsLogsInfo.CallsLogsInfoEntry.TABLE_NAME, CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_RUN_ID,
                CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_LOG_PERMISSION, CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_FOUND)!!)
        list.addAll(generateCallLogsReport()!!)

        list.addAll(generateCallsInfoReport(ContactsInfo.ContactsInfoEntry.TABLE_NAME, ContactsInfo.ContactsInfoEntry.COLUMN_NAME_RUN_ID,
                ContactsInfo.ContactsInfoEntry.COLUMN_NAME_CONTACTS_PERMISSION, ContactsInfo.ContactsInfoEntry.COLUMN_NAME_FOUND)!!)
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

    private fun generateCallsInfoReport(tableName: String, runIdColumn : String, permissionColumn: String, foundColumn: String): ArrayList<SubtaskStatus>? {
        val projection = arrayOf(
                permissionColumn,
                foundColumn
        )
        val db = DbHelper.getInstance(context)!!.readableDatabase
        val cursor = db.query(
                tableName,
                projection,
                "$runIdColumn=?",
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

    override fun onPostExecute(result: ArrayList<SubtaskStatus>?) {
        progDailog.dismiss();
        response.processFinish(result!!)

    }
    private fun generateList(cursor: Cursor?, projection: Array<String>) : ArrayList<SubtaskStatus>{
        val list = ArrayList<SubtaskStatus>()
        for (task in projection){
            Log.i("TASK", cursor!!.getColumnIndex(task).toString())
            list.add(SubtaskStatus(
                    task.replace("_", " "),
                    cursor!!.getInt(cursor.getColumnIndex(task)).toBoolean()).toEmoji())
        }
        return list
    }

}
