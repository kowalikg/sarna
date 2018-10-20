package pl.edu.agh.sarna.report

import android.app.ProgressDialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.provider.BaseColumns
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.model.WifiPasswords
import pl.edu.agh.sarna.db.model.calls.CallsDetails
import pl.edu.agh.sarna.db.scripts.singleMethodReport
import pl.edu.agh.sarna.db.scripts.updateProcess
import pl.edu.agh.sarna.model.AsyncResponse
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.utils.kotlin.toBoolean
import java.util.*

class DbReportTask(val context: Context, val response: AsyncResponse, val processID: Long, val rootAllowed: Boolean)  : AsyncTask<Void, Void, ArrayList<SubtaskStatus>>()  {
    private var progDailog = ProgressDialog(context)
    override fun onPreExecute() {
        progDailog.setMessage("Loading...")
        progDailog.isIndeterminate = false
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progDailog.setCancelable(true)
        progDailog.show()
    }
    override fun doInBackground(vararg p0: Void?): ArrayList<SubtaskStatus> {
        updateProcess(context, processID);
        val list = ArrayList<SubtaskStatus>()

        val db = DbHelper.getInstance(context)!!.readableDatabase

        if (rootAllowed) list.add(wifiPassword(db)!!)

        list.add(this.metadata(db)!!)
        return list
    }

    override fun onPostExecute(result: ArrayList<SubtaskStatus>?) {
        progDailog.dismiss();
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