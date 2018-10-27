package pl.edu.agh.sarna.smsToken.task

import android.app.ProgressDialog
import android.content.Context
import android.database.Cursor
import android.os.AsyncTask
import android.provider.BaseColumns
import android.util.Log
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.model.calls.CallsLogsInfo
import pl.edu.agh.sarna.db.model.contacts.ContactsInfo
import pl.edu.agh.sarna.db.model.smsToken.TokenSmsDetails
import pl.edu.agh.sarna.db.scripts.callLogsAmount
import pl.edu.agh.sarna.db.scripts.contactsAmount
import pl.edu.agh.sarna.db.scripts.mostFrequentContact
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.toBoolean


class TokenReportTask(val context: Context, val response: AsyncResponse, val runID: Long) : AsyncTask<Void, Void, ArrayList<SubtaskStatus>>() {
    private var progDialog = ProgressDialog(context)

    override fun onPreExecute() {
        progDialog.setMessage("Loading...")
        progDialog.isIndeterminate = false
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progDialog.setCancelable(true)
        progDialog.show()
    }

    override fun doInBackground(vararg p0: Void?): ArrayList<SubtaskStatus>? {
        val list = ArrayList<SubtaskStatus>()
        return list
    }

    private fun generateTokenReport(): ArrayList<SubtaskStatus>? {
        val projection = arrayOf(
                TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_MODE,
                TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_STATUS
        )
        val db = DbHelper.getInstance(context)!!.readableDatabase
        val cursor = db.query(
                TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME,
                projection,
                "${BaseColumns._ID}=?",
                arrayOf("$runID"),
                null, null,
                null
        )
        val list = ArrayList<SubtaskStatus>()
        if (cursor.moveToFirst()) {
            list.addAll(generateList(cursor, projection))
        }
        return list
    }

    override fun onPostExecute(result: ArrayList<SubtaskStatus>?) {
        progDialog.dismiss();
        response.processFinish(result!!)

    }
    private fun generateList(cursor: Cursor?, projection: Array<String>) : ArrayList<SubtaskStatus>{
        val list = ArrayList<SubtaskStatus>()
        for (task in projection){
            Log.i("TASK", cursor!!.getColumnIndex(task).toString())
            when(task){
                TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_MODE
                    ->
                        list.add(SubtaskStatus(
                            task.replace("_", " "),
                            Mode.values()[cursor.getInt(cursor.getColumnIndex(task))].toString()))
                else -> list.add(SubtaskStatus(
                        task.replace("_", " "),
                        cursor.getInt(cursor.getColumnIndex(task)).toBoolean()).toEmoji())
            }

        }
        return list
    }

}
