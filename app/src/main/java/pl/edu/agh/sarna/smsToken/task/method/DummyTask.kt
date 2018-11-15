package pl.edu.agh.sarna.smsToken.task.method

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import pl.edu.agh.sarna.db.scripts.insertSmsPermissions
import pl.edu.agh.sarna.db.scripts.insertTokenQuery
import pl.edu.agh.sarna.db.scripts.smsMethodProceed
import pl.edu.agh.sarna.db.scripts.updateTokenMethod
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.async.MethodAsyncTask
import pl.edu.agh.sarna.utils.kotlin.isDefaultSmsApp
import java.lang.ref.WeakReference


class DummyTask(contextReference: WeakReference<Context>,
                response: AsyncResponse,
                processID: Long,
                serverState: Boolean)
    : MethodAsyncTask(contextReference, response, processID, serverState) {
    override fun doInBackground(vararg p0: Void?): Int {
        saveSms(contextReference.get(), "112", "siema", "0", "", "inbox")
        val runID = insertTokenQuery(contextReference.get(), processID, Mode.DUMMY.ordinal)!!
        if (insertSmsPermissions(contextReference.get(), runID) < 0) return -1
        if (!isDefaultSmsApp(contextReference.get()!!)) updateTokenMethod(contextReference.get(), runID, false)
        else {
            waitForUpdate(runID)
        }
        return 0
    }

    private fun waitForUpdate(runID: Long) {
        while (!smsMethodProceed(contextReference.get(), runID)){
            Thread.sleep(200)
        }
    }
    fun saveSms(context: Context?, phoneNumber: String, message: String, readState: String, time: String, folderName: String): Boolean {
        var ret = false
        try {
            val values = ContentValues()
            values.put("address", phoneNumber)
            values.put("body", message)
            values.put("read", readState) //"0" for have not read sms and "1" for have read sms
            values.put("date", time)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                var uri = Telephony.Sms.Sent.CONTENT_URI
                if (folderName == "inbox") {
                    uri = Telephony.Sms.Inbox.CONTENT_URI
                }
                context!!.contentResolver.insert(uri, values)
            } else {
                /* folderName  could be inbox or sent */
                context!!.contentResolver.insert(Uri.parse("content://sms/$folderName"), values)
            }

            ret = true
        } catch (ex: Exception) {
            ex.printStackTrace()
            ret = false
        }

        return ret
    }
}