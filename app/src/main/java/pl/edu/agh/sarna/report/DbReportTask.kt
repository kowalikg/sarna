package pl.edu.agh.sarna.report

import android.app.ProgressDialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.provider.BaseColumns
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.DbScripts
import pl.edu.agh.sarna.db.model.WifiPasswords
import pl.edu.agh.sarna.model.AsyncResponse
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.utils.kotlin.toBoolean

class DbReportTask(val context: Context, val response: AsyncResponse, val processID: Long)  : AsyncTask<Void, Void, ArrayList<SubtaskStatus>>()  {
    private var progDailog = ProgressDialog(context)
    override fun onPreExecute() {
        progDailog.setMessage("Loading...")
        progDailog.isIndeterminate = false
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progDailog.setCancelable(true)
        progDailog.show()
    }
    override fun doInBackground(vararg p0: Void?): ArrayList<SubtaskStatus> {
        DbScripts.updateProcess(context, processID);
        val list = ArrayList<SubtaskStatus>()

        val db = DbHelper.getInstance(context)!!.readableDatabase
        list.add(wifiPassword(db)!!)
        return list
    }

    private fun wifiPassword(db: SQLiteDatabase?) : SubtaskStatus? {
        val cursor = db!!.query(
                WifiPasswords.WifiPasswordsEntry.TABLE_NAME,
                arrayOf(BaseColumns._ID, WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_STATUS),
                "${WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_PROCESS_ID}=?",
                arrayOf(processID.toString()),
                null, null,
                "_id DESC" ,
                "1"
        )
        if (cursor.moveToFirst()) {
            return SubtaskStatus(
                        cursor!!.getLong(cursor.getColumnIndex(BaseColumns._ID)),
                        context.getString(R.string.wifi_title),
                        cursor.getInt(cursor.getColumnIndex(WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_STATUS)).toBoolean())
        }
        return null
    }


    override fun onPostExecute(result: ArrayList<SubtaskStatus>?) {
        progDailog.dismiss();
        response.load(result!!)

    }
}