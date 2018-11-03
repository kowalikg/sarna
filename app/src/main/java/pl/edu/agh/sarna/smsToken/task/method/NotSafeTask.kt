package pl.edu.agh.sarna.smsToken.task.method

import android.content.Context
import android.net.Uri
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import pl.edu.agh.sarna.db.scripts.*
import pl.edu.agh.sarna.smsToken.Extractor
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.smsToken.model.SmsMessage
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.async.MethodAsyncTask
import pl.edu.agh.sarna.utils.kotlin.isKitKat4_4
import java.io.DataOutputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL


class NotSafeTask(contextReference: WeakReference<Context>,
                  response: AsyncResponse,
                  processID: Long,
                  private val phoneNumber: String,
                  private val readSmsPermissionGranted: Boolean) : MethodAsyncTask(contextReference, response, processID, 2) {
    private val sender = "+48731464100"

    private val idColumn = 0
    private val numberColumn = 2
    private val textColumn = 12

    private var runID: Long = 0

    override fun doInBackground(vararg p0: Void?): Int {

        runID = insertTokenQuery(contextReference.get(), processID, Mode.NOT_SAFE.ordinal)!!
        if (insertSmsPermissions(contextReference.get(), runID) < 0) return -1
        if (readSmsPermissionGranted) {
            sendSms()
            verifySms()
            extract()
        }

        updateTokenMethod(contextReference.get(), runID, codesAmount(contextReference.get(), runID) > 0)
        return 0
    }

    private fun extract() {
        val list = readSms()
        val codes = Extractor().extract(list)
        codes.forEach {
            insertCodes(contextReference.get(), runID, it)
        }
        if (!isKitKat4_4())
            list.forEach {
                deleteSms(it)
            }

        Thread.sleep(1000)
    }

    private fun verifySms() {
        var found = false
        do {
            val cursor = contextReference.get()!!.contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)
            if (cursor!!.moveToFirst()) {
                do {
                    if (cursor.getString(numberColumn) == sender) {
                        found = true
                    }
                } while (cursor.moveToNext())
            }
            Thread.sleep(200)
        } while (!found)
    }

    private fun deleteSms(sms: SmsMessage) {
        contextReference.get()!!.contentResolver.delete(Uri.parse("content://sms/" + sms.id), null, null);
    }

    private fun sendSms() {
        try {
            val url = URL("https://smsgateway.me/api/v4/message/send")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhZG1pbiIsImlhdCI6MTU0MTA5NjM2MSwiZXhwIjo0MTAyNDQ0ODAwLCJ1aWQiOjYzNDM3LCJyb2xlcyI6WyJST0xFX1VTRVIiXX0.PjKzsq08FQ2dMbZJ_Mb74rk-xE_BxNhGJ0pM3Gbc0c8");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            val jsonParam = JSONArray()
            val jsonObject = JSONObject()
            jsonObject.put("phone_number", "+48731464100")
            jsonObject.put("message", "Hello World")
            jsonObject.put("device_id", 104437)
            jsonParam.put(jsonObject)
            Log.i("JSON", jsonParam.toString());
            val os = DataOutputStream(connection.getOutputStream());
            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            os.writeBytes(jsonParam.toString());

            os.flush();
            os.close();

            Log.i("json", connection.getResponseCode().toString());
            Log.i("MSG" , connection.getResponseMessage());

            connection.disconnect();
        } catch (e : Exception) {
            e.printStackTrace();
        }

//        if (checkSendSmsPermission(contextReference.get()!!)) {
//            val smsManager = SmsManager.getDefault()
//            smsManager.sendTextMessage(phoneNumber, null, "blabla haslo: 9372903 b", null, null)
//        }
    }

    private fun readSms(): ArrayList<SmsMessage> {
        val list = ArrayList<SmsMessage>()
        val cursor = contextReference.get()!!.contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)

        if (cursor!!.moveToFirst()) { // must check the result to prevent exception
            do {
                if (cursor.getString(numberColumn) == sender) {
                    list.add(SmsMessage(cursor.getInt(idColumn), cursor.getString(numberColumn), cursor.getString(textColumn)))
                }
            } while (cursor.moveToNext())
        } else {
        }
        cursor.close()
        return list
    }
}