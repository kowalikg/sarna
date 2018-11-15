package pl.edu.agh.sarna.smsToken.task.method

import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.service.autofill.Validators.and
import android.util.Log
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.scripts.*
import pl.edu.agh.sarna.permissions.checkReadSmsPermission
import pl.edu.agh.sarna.smsToken.Extractor
import pl.edu.agh.sarna.smsToken.SmsSender
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.smsToken.model.SmsMessage
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.async.MethodAsyncTask
import pl.edu.agh.sarna.utils.kotlin.isKitKat4_4
import java.lang.ref.WeakReference


class NotSafeTask(contextReference: WeakReference<Context>,
                  response: AsyncResponse,
                  processID: Long,
                  serverState: Boolean,
                  private val phoneNumber: String, private val mode: Mode) : MethodAsyncTask(contextReference, response, processID, serverState) {
    private val sender = "+48731464100"

    private val idColumn = 0
    private val numberColumn = if (isKitKat4_4()) 2 else 3
    private val textColumn = if (isKitKat4_4()) 12 else 13

    private var runID: Long = 0

    override fun doInBackground(vararg p0: Void?): Int {
        if (mode == Mode.TEST){
            if(SmsSender(contextReference, serverState).sendSms(phoneNumber)){
                if (verifySms()){
                    return mode.ordinal
                }
                return -1
            }
        }
        else {
            runID = insertTokenQuery(contextReference.get(), processID, Mode.NOT_SAFE.ordinal)!!
            if (insertSmsPermissions(contextReference.get(), runID) < 0) return -1
            if (checkReadSmsPermission(contextReference.get()!!))
                if(verifySms()) extract()
            updateTokenMethod(contextReference.get(), runID, codesAmount(contextReference.get(), runID) > 0)
            return Mode.NOT_SAFE.ordinal
        }
        return -1
    }

    private fun extract() {
        val list = readSms()
        val codes = Extractor(contextReference).extract(list)
        Log.i("CODE", codes.toString())
        codes.forEach {
            insertCodes(contextReference.get(), runID, it)
        }
        if (!isKitKat4_4()){
            list.forEach {
                deleteSms(it)
            }
            val notificationManager = contextReference.get()!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }

        Thread.sleep(1000)
    }

    private fun verifySms() : Boolean {

        var found = false
        do {
            Thread.sleep(1000)
            val cursor = contextReference.get()!!.contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)
            if (cursor!!.moveToFirst()) {
                do {
                    if (containsCode(cursor.getString(numberColumn), cursor.getString(textColumn))) {
                        found = true
                    }
                } while (cursor.moveToNext() and !found)
            }
        } while (!found)
        return found
    }

    private fun deleteSms(sms: SmsMessage) {
        contextReference.get()!!.contentResolver.delete(Uri.parse("content://sms/" + sms.id), null, null);
    }

    private fun readSms(): ArrayList<SmsMessage> {
        val list = ArrayList<SmsMessage>()
        val cursor = contextReference.get()!!.contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)

        if (cursor!!.moveToFirst()) {
            do {
                if (containsCode(cursor.getString(numberColumn), cursor.getString(textColumn))) {
                    list.add(SmsMessage(cursor.getInt(idColumn), cursor.getString(numberColumn), cursor.getString(textColumn)))
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    private fun containsCode(number: String, body: String) : Boolean {
        if ((mode == Mode.NOT_SAFE) and (number in arrayOf(sender, phoneNumber))) return false
        if ((mode == Mode.TEST) and (number != sender)) return false
        val pattern = """${contextReference.get()!!.getString(R.string.password_regexp)}(\d+)"""
        val regex = pattern.toRegex()
        regex.find(body)?.let { return true } ?: run { return false }
    }
}