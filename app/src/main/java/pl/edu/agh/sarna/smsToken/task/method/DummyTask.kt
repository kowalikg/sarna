package pl.edu.agh.sarna.smsToken.task.method

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.scripts.insertSmsPermissions
import pl.edu.agh.sarna.db.scripts.insertTokenQuery
import pl.edu.agh.sarna.db.scripts.smsMethodProceed
import pl.edu.agh.sarna.db.scripts.updateTokenMethod
import pl.edu.agh.sarna.smsToken.SmsSender
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.async.MethodAsyncTask
import pl.edu.agh.sarna.utils.kotlin.isDefaultSmsApp
import pl.edu.agh.sarna.utils.kotlin.isKitKat4_4
import pl.edu.agh.sarna.utils.kotlin.isNougat7_1_2
import java.lang.ref.WeakReference


class DummyTask(contextReference: WeakReference<Context>,
                response: AsyncResponse,
                processID: Long,
                private val runID: Long,
                serverState: Boolean,
                private val phoneNumber: String,
                private val mode: Mode)
    : MethodAsyncTask(contextReference, response, processID, serverState) {
    private val sender = "+48731464100"
    private val numberColumn = if (isKitKat4_4()) 2 else 3
    private val textColumn = if (isNougat7_1_2()) 12 else 13
    override fun doInBackground(vararg p0: Void?): Int {
        if (mode == Mode.TEST_DUMMY){
            if(SmsSender(contextReference, serverState).sendSms(phoneNumber)){
                if(!waitForTestUpdate()) return -1
            }
        }
        else {
            if (insertSmsPermissions(contextReference.get(), runID) < 0) return -1
            if (!isDefaultSmsApp(contextReference.get()!!)) updateTokenMethod(contextReference.get(), runID, false)
            else {
                if(!waitForUpdate(runID)) return -1
            }
        }
        return mode.ordinal
    }

    private fun waitForTestUpdate() : Boolean{
        var iteration = 0
        while (!testSmsArrived() and (iteration < 10)){
            iteration++
        }
        if(iteration == 10) return false
        return true
    }

    private fun testSmsArrived(): Boolean {
        val cursor = contextReference.get()!!.contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)
        var found = false
        if (cursor!!.moveToFirst()) {
            do {
                if (containsCode(cursor.getString(numberColumn), cursor.getString(textColumn))) {
                    found = true
                }
            } while (cursor.moveToNext() and !found)
            Thread.sleep(2000)
        }
        return found
    }

    private fun waitForUpdate(runID: Long) : Boolean {
        var iteration = 0
        while (!smsMethodProceed(contextReference.get(), runID) and (iteration < 10)){
            Thread.sleep(2000)
            iteration++
        }
        if(iteration == 10) return false
        return true
    }


    private fun containsCode(number: String, body: String) : Boolean {
        if ((mode == Mode.DUMMY) and (number in arrayOf(sender, phoneNumber))) return false
        if ((mode == Mode.TEST_DUMMY) and (number !in arrayOf(sender, phoneNumber))) return false
        val pattern = if (mode == Mode.DUMMY)
            """${contextReference.get()!!.getString(R.string.password_regexp)}(\d+)"""
        else """test:[ ]*(\d+)"""
        val regex = pattern.toRegex()
        regex.find(body)?.let { return true } ?: run { return false }
    }
}