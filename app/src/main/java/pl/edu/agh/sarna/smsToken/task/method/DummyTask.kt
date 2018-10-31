package pl.edu.agh.sarna.smsToken.task.method

import android.content.Context
import android.telephony.SmsManager
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.async.MethodAsyncTask
import java.lang.ref.WeakReference
import pl.edu.agh.sarna.db.scripts.*
import pl.edu.agh.sarna.permissions.checkSendSmsPermission
import pl.edu.agh.sarna.utils.kotlin.isDefaultSmsApp


class DummyTask(contextReference: WeakReference<Context>,
                response: AsyncResponse,
                processID: Long,
                private val phoneNumber: String,
                private val defaultSmsApp: Boolean)
    : MethodAsyncTask(contextReference, response, processID, 3) {
    override fun doInBackground(vararg p0: Void?): Int {
        val runID = insertTokenQuery(contextReference.get(), processID, Mode.DUMMY.ordinal)!!
        if (insertSmsPermissions(contextReference.get(), runID) < 0) return -1
        if (!defaultSmsApp) updateTokenMethod(contextReference.get(), runID, false)
        else {
            if(sendSms()) waitForUpdate(runID)
            updateTokenMethod(contextReference.get(), runID, false)
        }
        return 0
    }

    private fun waitForUpdate(runID: Long) {
        while (!smsMethodProceed(contextReference.get(), runID)) Thread.sleep(200)
    }

    private fun sendSms(): Boolean {
        if (checkSendSmsPermission(contextReference.get()!!)) {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, "blabla haslo: 9372903 b", null, null)
            return true
        }
        return false
    }
}