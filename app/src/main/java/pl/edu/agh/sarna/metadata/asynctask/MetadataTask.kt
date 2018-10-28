package pl.edu.agh.sarna.metadata.asynctask

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import org.bson.Document
import pl.edu.agh.sarna.db.model.calls.CallsDetails.CallsDetailsEntry
import pl.edu.agh.sarna.db.mongo.MongoDb
import pl.edu.agh.sarna.db.mongo.MongoDbException
import pl.edu.agh.sarna.db.scripts.*
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import java.util.*

class MetadataTask(val context: Context, val response: AsyncResponse, val processID: Long,
                   val callLogsPermissionGranted : Boolean, val contactsPermissionGranted : Boolean) : AsyncTask<Void, Void, Int>() {

    private val progDailog = ProgressDialog(context)
    private var runID : Long = 0

    override fun onPreExecute() {
        progDailog.setMessage("Loading...")
        progDailog.isIndeterminate = false
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progDailog.setCancelable(true)
        progDailog.show()
    }

    override fun doInBackground(vararg p0: Void?): Int {
        val startTime = Calendar.getInstance().timeInMillis
        runID = insertCallsQuery(context, processID, startTime)!!
        val callsStatus = doCallsJob() == TaskStatus.CALL_OK
        val contactsStatus = doContactsJob() == TaskStatus.CONTACTS_OK
        val status = callsStatus and contactsStatus
        val endTime = Calendar.getInstance().timeInMillis
        updateCallsMethod(context, runID, status, endTime)
        saveToMongo(runID, processID, startTime, endTime, status)
        return 0
    }

    private fun saveToMongo(runID: Long, processID: Long, startTime: Long, endTime: Long, status: Boolean) {
        val mongoDb = MongoDb()
        val document = Document()
                .append(CallsDetailsEntry.TABLE_KEY, runID)
                .append(CallsDetailsEntry.COLUMN_NAME_PROCESS_ID, processID)
                .append(CallsDetailsEntry.COLUMN_NAME_START_TIME, startTime)
                .append(CallsDetailsEntry.COLUMN_NAME_END_TIME, endTime)
                .append(CallsDetailsEntry.COLUMN_NAME_STATUS, status)
        try {
            mongoDb.saveData(CallsDetailsEntry.TABLE_NAME, document)
        } catch (e: MongoDbException) {
            Log.e("MongoDB", e.message)
        }
    }

    private fun doContactsJob() : TaskStatus {
        insertContactsInfoQuery(context, runID, contactsPermissionGranted)
        if (contactsPermissionGranted)
            if (getAccessToContacts(runID) == TaskStatus.CONTACTS_ERROR) {
                updateContactsInfoQuery(context, runID, false)
                return TaskStatus.CONTACTS_ERROR
            }

        val amount = contactsAmount(context, runID)
        updateContactsInfoQuery(context, runID, amount > 0)

        if (amount > 0) return TaskStatus.CONTACTS_OK
        return TaskStatus.CONTACTS_ERROR
    }

    private fun getAccessToContacts(runID: Long?): TaskStatus {
        val phones = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        while (phones!!.moveToNext()) {
            val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            Log.i("CONTACTS", "$name $phoneNumber")
            val status = insertContacts(context, runID!!, name, phoneNumber)
            if (status.toInt() == -1) return TaskStatus.CONTACTS_ERROR

        }
        phones.close()
        return TaskStatus.CONTACTS_OK
    }

    @SuppressLint("MissingPermission")
    private fun doCallsJob() : TaskStatus {
        insertCallsLogsInfoQuery(context, runID, callLogsPermissionGranted)

        if (callLogsPermissionGranted)
            if (getAccessToCalls(runID) == TaskStatus.CALL_ERROR){
                updateCallsLogsInfoQuery(context, runID, false)
                return TaskStatus.CALL_ERROR
            }

        val amount = callLogsAmount(context, runID)
        updateCallsLogsInfoQuery(context, runID, amount > 0)

        if (amount > 0) return TaskStatus.CALL_OK
        return TaskStatus.CALL_ERROR
    }

    @SuppressLint("MissingPermission")
    private fun getAccessToCalls(runID : Long) : TaskStatus {
        val projection = arrayOf(CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE)
        val cursor = context.contentResolver.query(CallLog.Calls.CONTENT_URI, projection, null, null, null)
        while (cursor.moveToNext()) {
            val status = insertCallsLogs(context, runID, cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3))
            if (status.toInt() == -1) return TaskStatus.CALL_ERROR
        }
        cursor.close()
        return TaskStatus.CALL_OK
    }

    override fun onPostExecute(result: Int?) {
        progDailog.dismiss()
        response.processFinish(result!!)

    }
}
