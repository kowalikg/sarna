package pl.edu.agh.sarna.report.asynctask

import android.app.ProgressDialog
import android.content.Context
import android.database.Cursor
import android.os.AsyncTask
import android.provider.BaseColumns
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.toBoolean
import java.lang.ref.WeakReference

abstract class ReportTask(protected val contextReference: WeakReference<Context>, private val response : AsyncResponse) : AsyncTask<Void, Void, ArrayList<SubtaskStatus>>() {
    private var progressDialog = ProgressDialog(contextReference.get())

    override fun onPreExecute() {
        progressDialog.setMessage("Loading...")
        progressDialog.isIndeterminate = false
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(true)
        progressDialog.show()
    }
    abstract override fun doInBackground(vararg p0: Void?): ArrayList<SubtaskStatus>?
    override fun onPostExecute(result: ArrayList<SubtaskStatus>?) {
        progressDialog.dismiss();
        response.processFinish(result!!)

    }
    protected fun generateTableReport(runID : Long, tableName : String, projection: Array<String>, matchColumn : String = BaseColumns._ID ): ArrayList<SubtaskStatus>? {

        val db = DbHelper.getInstance(contextReference.get())!!.readableDatabase
        val cursor = db.query(
                tableName,
                projection,
                "$matchColumn=?",
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

    protected open fun generateList(cursor: Cursor?, projection: Array<String>) : ArrayList<SubtaskStatus> {
        val list = ArrayList<SubtaskStatus>()
        for (task in projection){
            list.add(SubtaskStatus(
                    task.replace("_", " "),
                    cursor!!.getInt(cursor.getColumnIndex(task)).toBoolean()).toEmoji())
        }
        return list
    }
}