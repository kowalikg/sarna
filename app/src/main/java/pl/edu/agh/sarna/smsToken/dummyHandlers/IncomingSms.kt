package pl.edu.agh.sarna.smsToken.dummyHandlers

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.scripts.getLastRunID
import pl.edu.agh.sarna.db.scripts.insertCodes
import pl.edu.agh.sarna.db.scripts.updateTokenMethod
import pl.edu.agh.sarna.smsToken.Extractor
import pl.edu.agh.sarna.smsToken.model.SmsMessage
import java.lang.ref.WeakReference
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.NotificationChannel






class IncomingSms : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onReceive(p0: Context?, p1: Intent?) {
        val runID = getLastRunID(p0)
        val codes = ArrayList<SmsMessage>()
        for (smsMessage in Telephony.Sms.Intents.getMessagesFromIntent(p1)) {
            if (!containsCode(p0!!, smsMessage.messageBody)){
                saveSms(p0, smsMessage.displayOriginatingAddress,
                        smsMessage.messageBody)
               showNotification(p0, smsMessage.displayOriginatingAddress, smsMessage.messageBody)
            }
            else {
            codes.add(Extractor(WeakReference(p0)).extract(SmsMessage(
                    0, smsMessage.displayOriginatingAddress, smsMessage.messageBody))!!)

            }
        }
        if (!codes.isEmpty()){
            codes.forEach {  insertCodes(p0!!, runID, it)}
            updateTokenMethod(p0, runID, !codes.isEmpty())
        }
    }
    private fun containsCode(context: Context, body: String) : Boolean {
        val pattern = """${context.getString(R.string.password_regexp)}(\d+)"""
        val regex = pattern.toRegex()
        regex.find(body)?.let { return true } ?: run { return false }
    }
    fun saveSms(context: Context?, phoneNumber: String, message: String): Boolean {
        var ret : Boolean
        try {
            val values = ContentValues()
            values.put("address", phoneNumber)
            values.put("body", message)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                context!!.contentResolver.insert(Uri.parse("content://sms/"), values)
            } else {
                /* folderName  could be inbox or sent */
                context!!.contentResolver.insert(Uri.parse("content://sms/inbox"), values)
            }

            ret = true
        } catch (ex: Exception) {
            ex.printStackTrace()
            ret = false
        }

        return ret
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
                .setSmallIcon(R.mipmap.sms_black) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(content)// message for notification
                .setAutoCancel(true) // clear notification after click
        mNotificationManager.notify(0, mBuilder.build())
    }
}