package pl.edu.agh.sarna.metadata

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import android.provider.CallLog
import android.util.Log
import pl.edu.agh.sarna.model.AsyncResponse

class CallLogsTask(private val context: Activity, private val response: AsyncResponse, val processID: Long, private var callLogsPermissionGranted: Boolean = false) : AsyncTask<Void, Void, TaskStatus>() {
    private val progDailog = ProgressDialog(context)
    override fun onPreExecute() {
        progDailog.setMessage("Loading...")
        progDailog.isIndeterminate = false
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progDailog.setCancelable(true)
        progDailog.show()
    }
    @SuppressLint("MissingPermission")
    override fun doInBackground(vararg p0: Void?): TaskStatus? {
        val projection = arrayOf(CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE)
        val cursor = context.contentResolver.query(CallLog.Calls.CONTENT_URI, projection, null, null, null)
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val number = cursor.getString(1)
            val type = cursor.getString(2) // https://developer.android.com/reference/android/provider/CallLog.Calls.html#TYPE
            val time = cursor.getString(3) // epoch time - https://developer.android.com/reference/java/text/DateFormat.html#parse(java.lang.String
            Log.i("META", "$name $number $type $time")
        }
        cursor.close()
        Thread.sleep(1000)
        return TaskStatus.CALL_OK
    }
    override fun onPostExecute(result: TaskStatus?) {
        progDailog.dismiss();
        response.processFinish(result!!)

    }
}