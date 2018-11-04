package pl.edu.agh.sarna.smsToken

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import pl.edu.agh.sarna.permissions.checkSendSmsPermission
import pl.edu.agh.sarna.utils.kotlin.isNetworkAvailable
import java.io.DataOutputStream
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

class SmsSender(val contextReference: WeakReference<Context>, private val serverState: Boolean) {
    fun sendSms(phoneNumber: String): Boolean {
        return if (serverState and isNetworkAvailable(contextReference.get()!!)) sendViaServer(phoneNumber)
        else sendByYourself(phoneNumber)

    }

    private fun sendViaServer(phoneNumber: String): Boolean {
        try {
            val url = URL("https://smsgateway.me/api/v4/message/send")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST";
            connection.setRequestProperty("Authorization", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhZG1pbiIsImlhdCI6MTU0MTA5NjM2MSwiZXhwIjo0MTAyNDQ0ODAwLCJ1aWQiOjYzNDM3LCJyb2xlcyI6WyJST0xFX1VTRVIiXX0.PjKzsq08FQ2dMbZJ_Mb74rk-xE_BxNhGJ0pM3Gbc0c8");
            connection.doOutput = true;
            connection.doInput = true;

            val jsonParam = JSONArray()
            val jsonObject = JSONObject()
            jsonObject.put("phone_number", phoneNumber)
            jsonObject.put("message", "haslo: 434543543")
            jsonObject.put("device_id", 104437)
            jsonParam.put(jsonObject)
            Log.i("JSON", jsonParam.toString());
            val os = DataOutputStream(connection.getOutputStream());
            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            os.writeBytes(jsonParam.toString());

            os.flush();
            os.close();

            Log.i("json", connection.getResponseCode().toString());
            Log.i("MSG", connection.getResponseMessage());

            connection.disconnect();
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun sendByYourself(phoneNumber: String): Boolean {
        if (checkSendSmsPermission(contextReference.get()!!)) {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, "blabla haslo: 9372903 b", null, null)
            return true
        }
        return false
    }
}
