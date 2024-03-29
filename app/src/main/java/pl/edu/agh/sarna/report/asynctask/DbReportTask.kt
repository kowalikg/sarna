package pl.edu.agh.sarna.report.asynctask

import android.app.ProgressDialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.model.calls.CallsDetails
import pl.edu.agh.sarna.db.model.cloak.CloakInfo
import pl.edu.agh.sarna.db.model.dirtycow.DirtyCowDetails
import pl.edu.agh.sarna.db.model.wifi.WifiPasswords
import pl.edu.agh.sarna.db.scripts.singleMethodReport
import pl.edu.agh.sarna.db.scripts.smsMethodReport
import pl.edu.agh.sarna.db.scripts.updateProcess
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import java.lang.ref.WeakReference
import java.util.*

class DbReportTask(private val contextReference: WeakReference<Context>, val response: AsyncResponse, val processID: Long, val rootAllowed: Boolean)  : AsyncTask<Void, Void, ArrayList<SubtaskStatus>>()  {
    private var progressDialog = ProgressDialog(contextReference.get())
    override fun onPreExecute() {
        progressDialog.setMessage(contextReference.get()!!.getString(R.string.loading))
        progressDialog.isIndeterminate = false
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(true)
        progressDialog.show()
    }

    override fun doInBackground(vararg p0: Void?): ArrayList<SubtaskStatus> {
        updateProcess(contextReference.get()!!, processID);
        val list = ArrayList<SubtaskStatus>()

        val db = DbHelper.getInstance(contextReference.get())!!.readableDatabase

        list.add(wifiPassword(db)!!)
        list.add(cloak(db)!!)
        list.add(dirtycow(db)!!)
        list.add(tokenSms(db, Mode.NOT_SAFE)!!)
        list.add(tokenSms(db, Mode.DUMMY)!!)
        list.add(metadata(db)!!)

        return list
    }

    private fun cloak(db: SQLiteDatabase?): SubtaskStatus? {
        return singleMethodReport(db, processID, CloakInfo.CloakInfoEntry.TABLE_NAME,
                CloakInfo.CloakInfoEntry.COLUMN_NAME_STATUS,
                CloakInfo.CloakInfoEntry.COLUMN_NAME_PROCESS_ID,
                contextReference.get()!!.getString(R.string.cloak_title)
        )
    }

    private fun tokenSms(db: SQLiteDatabase?, mode: Mode): SubtaskStatus? {
        val title = if(mode == Mode.NOT_SAFE) contextReference.get()!!.getString(R.string.token_mode_not_safe)
        else contextReference.get()!!.getString(R.string.token_mode_for_dummies)

        return smsMethodReport(db, processID, mode, title)
    }
    private fun dirtycow(db: SQLiteDatabase?): SubtaskStatus? {
        return singleMethodReport(db, processID, DirtyCowDetails.DirtyCowDetailsEntry.TABLE_NAME,
                DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_STATUS,
                DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_PROCESS_ID,
                contextReference.get()!!.getString(R.string.dirtycow_title)
                )
    }
    private fun metadata(db: SQLiteDatabase?): SubtaskStatus? {
        return singleMethodReport(db, processID, CallsDetails.CallsDetailsEntry.TABLE_NAME, CallsDetails.CallsDetailsEntry.COLUMN_NAME_STATUS,
                CallsDetails.CallsDetailsEntry.COLUMN_NAME_PROCESS_ID, contextReference.get()!!.getString(R.string.metadata_title))
    }

    private fun wifiPassword(db: SQLiteDatabase?) : SubtaskStatus? {
        return singleMethodReport(db, processID, WifiPasswords.WifiPasswordsEntry.TABLE_NAME, WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_STATUS,
                WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_PROCESS_ID, contextReference.get()!!.getString(R.string.wifi_title))
    }
    override fun onPostExecute(result: ArrayList<SubtaskStatus>?) {
        progressDialog.dismiss();
        response.processFinish(result!!)
    }

}