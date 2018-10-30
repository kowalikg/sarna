package pl.edu.agh.sarna.report.asynctask

import android.app.ProgressDialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.model.calls.CallsDetails
import pl.edu.agh.sarna.db.model.smsToken.TokenSmsDetails
import pl.edu.agh.sarna.db.model.wifi.WifiPasswords
import pl.edu.agh.sarna.db.scripts.singleMethodReport
import pl.edu.agh.sarna.db.scripts.updateProcess
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import java.util.*

class DbReportTask(val context: Context, val response: AsyncResponse, val processID: Long, val rootAllowed: Boolean)  : AsyncTask<Void, Void, ArrayList<SubtaskStatus>>()  {
    private var progressDialog = ProgressDialog(context)
    override fun onPreExecute() {
        progressDialog.setMessage("Loading...")
        progressDialog.isIndeterminate = false
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(true)
        progressDialog.show()
    }
    override fun doInBackground(vararg p0: Void?): ArrayList<SubtaskStatus> {
        updateProcess(context, processID);
        val list = ArrayList<SubtaskStatus>()

        val db = DbHelper.getInstance(context)!!.readableDatabase

        if (rootAllowed) list.add(wifiPassword(db)!!)

        list.add(metadata(db)!!)
        list.add(tokenSms(db)!!)

        return list
    }

    private fun tokenSms(db: SQLiteDatabase?): SubtaskStatus? {
        return singleMethodReport(db, processID, TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME, TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_STATUS,
                TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_PROCESS_ID, context.getString(R.string.token_title))
    }

    override fun onPostExecute(result: ArrayList<SubtaskStatus>?) {
        progressDialog.dismiss();
        response.load(result!!)
    }

    private fun metadata(db: SQLiteDatabase?): SubtaskStatus? {
        return singleMethodReport(db, processID, CallsDetails.CallsDetailsEntry.TABLE_NAME, CallsDetails.CallsDetailsEntry.COLUMN_NAME_STATUS,
                CallsDetails.CallsDetailsEntry.COLUMN_NAME_PROCESS_ID, context.getString(R.string.metadata_title))
    }

    private fun wifiPassword(db: SQLiteDatabase?) : SubtaskStatus? {
        return singleMethodReport(db, processID, WifiPasswords.WifiPasswordsEntry.TABLE_NAME, WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_STATUS,
                WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_PROCESS_ID, context.getString(R.string.wifi_title))
    }


}