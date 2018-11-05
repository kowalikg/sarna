package pl.edu.agh.sarna.smsToken.task.method

import android.content.Context
import pl.edu.agh.sarna.db.scripts.insertSmsPermissions
import pl.edu.agh.sarna.db.scripts.insertTokenQuery
import pl.edu.agh.sarna.db.scripts.smsMethodProceed
import pl.edu.agh.sarna.db.scripts.updateTokenMethod
import pl.edu.agh.sarna.smsToken.SmsSender
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.async.MethodAsyncTask
import pl.edu.agh.sarna.utils.kotlin.isDefaultSmsApp
import java.lang.ref.WeakReference


class DummyTask(contextReference: WeakReference<Context>,
                response: AsyncResponse,
                processID: Long,
                serverState: Boolean,
                private val phoneNumber: String)
    : MethodAsyncTask(contextReference, response, processID, serverState, Mode.DUMMY.ordinal) {
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