package pl.edu.agh.sarna.smsToken.task.method

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.async.MethodAsyncTask
import java.lang.ref.WeakReference
import pl.edu.agh.sarna.db.scripts.*
import pl.edu.agh.sarna.smsToken.SmsSender
import pl.edu.agh.sarna.utils.kotlin.isDefaultSmsApp
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL


class DummyTask(contextReference: WeakReference<Context>,
                response: AsyncResponse,
                processID: Long,
                serverState: Boolean,
                private val phoneNumber: String)
    : MethodAsyncTask(contextReference, response, processID, serverState, 3) {
    override fun doInBackground(vararg p0: Void?): Int {
        val runID = insertTokenQuery(contextReference.get(), processID, Mode.DUMMY.ordinal)!!
        if (insertSmsPermissions(contextReference.get(), runID) < 0) return -1
        if (!isDefaultSmsApp(contextReference.get()!!)) updateTokenMethod(contextReference.get(), runID, false)
        else {
            if(SmsSender(contextReference, serverState).sendSms(phoneNumber)) waitForUpdate(runID)
        }
        return 0
    }

    private fun waitForUpdate(runID: Long) {
        while (!smsMethodProceed(contextReference.get(), runID)) Thread.sleep(200)
    }
}