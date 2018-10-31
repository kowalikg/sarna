package pl.edu.agh.sarna.metadata.asynctask

import android.annotation.SuppressLint
import android.content.Context
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import pl.edu.agh.sarna.db.scripts.*
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.async.MethodAsyncTask
import java.lang.ref.WeakReference

class MetadataTaskMethod(contextReference: WeakReference<Context>,
                         response: AsyncResponse,
                         processID: Long,
                         private val callLogsPermissionGranted : Boolean,
                         private val contactsPermissionGranted : Boolean) : MethodAsyncTask(contextReference, response, processID) {
    private var runID : Long = 0

    override fun doInBackground(vararg p0: Void?): Int {
        runID = insertCallsQuery(contextReference.get(), processID)!!
        val callsStatus = doCallsJob() == TaskStatus.CALL_OK
        val contactsStatus = doContactsJob() == TaskStatus.CONTACTS_OK
        updateCallsMethod(contextReference.get(), runID, callsStatus and contactsStatus)
        return 0
    }

    private fun doContactsJob() : TaskStatus {
        insertContactsInfoQuery(contextReference.get(), runID, contactsPermissionGranted)
        if (contactsPermissionGranted)
            if (getAccessToContacts(runID) == TaskStatus.CONTACTS_ERROR) {
                updateContactsInfoQuery(contextReference.get(), runID, false)
                return TaskStatus.CONTACTS_ERROR
            }

        val amount = contactsAmount(contextReference.get(), runID)
        updateContactsInfoQuery(contextReference.get(), runID, amount > 0)

        if (amount > 0) return TaskStatus.CONTACTS_OK
        return TaskStatus.CONTACTS_ERROR
    }

    private fun getAccessToContacts(runID: Long?): TaskStatus {
        val phones = contextReference.get()!!.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        while (phones!!.moveToNext()) {
            val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            Log.i("CONTACTS", "$name $phoneNumber")
            val status = insertContacts(contextReference.get(), runID!!, name, phoneNumber)
            if (status.toInt() == -1) return TaskStatus.CONTACTS_ERROR

        }
        phones.close()
        return TaskStatus.CONTACTS_OK
    }

    @SuppressLint("MissingPermission")
    private fun doCallsJob() : TaskStatus {
        insertCallsLogsInfoQuery(contextReference.get(), runID, callLogsPermissionGranted)

        if (callLogsPermissionGranted)
            if (getAccessToCalls(runID) == TaskStatus.CALL_ERROR){
                updateCallsLogsInfoQuery(contextReference.get(), runID, false)
                return TaskStatus.CALL_ERROR
            }

        val amount = callLogsAmount(contextReference.get(), runID)
        updateCallsLogsInfoQuery(contextReference.get(), runID, amount > 0)

        if (amount > 0) return TaskStatus.CALL_OK
        return TaskStatus.CALL_ERROR
    }

    @SuppressLint("MissingPermission")
    private fun getAccessToCalls(runID : Long) : TaskStatus {
        val projection = arrayOf(CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE)
        val cursor = contextReference.get()!!.contentResolver.query(CallLog.Calls.CONTENT_URI, projection, null, null, null)
        while (cursor.moveToNext()) {
            val status = insertCallsLogs(contextReference.get(), runID, cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3))
            if (status.toInt() == -1) return TaskStatus.CALL_ERROR
        }
        cursor.close()
        return TaskStatus.CALL_OK
    }

}