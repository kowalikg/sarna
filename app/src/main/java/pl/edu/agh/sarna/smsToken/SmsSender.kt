package pl.edu.agh.sarna.smsToken

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.permissions.checkSendSmsPermission
import pl.edu.agh.sarna.utils.kotlin.isNetworkAvailable
import java.io.DataOutputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class SmsSender(val contextReference: WeakReference<Context>, private val serverState: Boolean) {
    private val apiKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhZG1pbiIsImlhdCI6MTU0MTA5NjM2MSwiZXhwIjo0MTAyNDQ0ODAwLCJ1aWQiOjYzNDM3LCJyb2xlcyI6WyJST0xFX1VTRVIiXX0.PjKzsq08FQ2dMbZJ_Mb74rk-xE_BxNhGJ0pM3Gbc0c8"
    private val deviceID = 104437
    private val uri = "https://smsgateway.me/api/v4/message/send"

    fun sendSms(phoneNumber: String): Boolean {
        return if (serverState and isNetworkAvailable(contextReference.get()!!)) sendViaServer(phoneNumber)
        else sendByYourself(phoneNumber)
    }
    
    private fun nextPassword() = Math.abs(Random().nextInt() % 1000000) + 1000000

    private fun sendViaServer(phoneNumber: String): Boolean {
        try {
            val url = URL(uri)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", apiKey)
            connection.doOutput = true
            connection.doInput = true

            val jsonParam = JSONArray()
            val jsonObject = JSONObject()
            jsonObject.put("phone_number", phoneNumber)
            jsonObject.put("message", "test:${nextPassword()}")
            jsonObject.put("device_id", deviceID)
            jsonParam.put(jsonObject)

            val os = DataOutputStream(connection.outputStream)
            os.writeBytes(jsonParam.toString())
            Log.i("MSG" , connection.getResponseMessage());
            os.flush();
            os.close();

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
            smsManager.sendTextMessage(phoneNumber, null,
                    "test:${nextPassword()}",
                    null, null)
            return true
        }
        return false
    }
}
