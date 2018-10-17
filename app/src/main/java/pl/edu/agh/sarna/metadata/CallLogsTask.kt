package pl.edu.agh.sarna.metadata

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import android.provider.CallLog
import pl.edu.agh.sarna.db.DbScripts
import pl.edu.agh.sarna.model.AsyncResponse

class CallLogsTask(private val context: Activity, private val response: AsyncResponse, val runID: Long, private var callLogsPermissionGranted: Boolean = false) : AsyncTask<Void, Void, TaskStatus>() {
    private val progDailog = ProgressDialog(context)
    private var tryID : Long = 0
    override fun onPreExecute() {
        progDailog.setMessage("Loading...")
        progDailog.isIndeterminate = false
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progDailog.setCancelable(true)
        progDailog.show()
    }
    @SuppressLint("MissingPermission")
    override fun doInBackground(vararg p0: Void?): TaskStatus? {
        tryID = DbScripts.insertCallsLogsInfoQuery(context, runID, callLogsPermissionGranted)
        val projection = arrayOf(CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE)
        val cursor = context.contentResolver.query(CallLog.Calls.CONTENT_URI, projection, null, null, null)
        while (cursor.moveToNext()) {
            val status = DbScripts.insertCallsLogs(context, tryID, cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3))
            if (status.toInt() == -1) return TaskStatus.CALL_ERROR
        }
        cursor.close()
        val amount = DbScripts.callLogsAmount(context, tryID)
        DbScripts.updateCallsLogsInfoQuery(context, runID, amount > 0)
        Thread.sleep(1000)
        if (amount > 0) return TaskStatus.CALL_OK
        return TaskStatus.CALL_ERROR
    }
    override fun onPostExecute(result: TaskStatus?) {
        progDailog.dismiss();
        response.processFinish(result!!)

    }
}