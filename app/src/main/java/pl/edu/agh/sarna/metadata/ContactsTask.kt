package pl.edu.agh.sarna.metadata

import android.app.Activity
import android.os.AsyncTask
import android.provider.ContactsContract
import android.util.Log
import pl.edu.agh.sarna.model.AsyncResponse

class ContactsTask(private val context: Activity, private val response: AsyncResponse, val processID: Long, private var contactsPermissionGranted: Boolean = false) : AsyncTask<Void, Void, TaskStatus>()  {
    override fun doInBackground(vararg p0: Void?): TaskStatus {
        val phones = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        while (phones!!.moveToNext()) {
            val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            Log.i("CONTACTS", "$name $phoneNumber")

        }
        phones.close()
        return TaskStatus.CONTACTS_OK
    }
    override fun onPostExecute(result: TaskStatus?) {
        response.processFinish(result!!)

    }
}