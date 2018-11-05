package pl.edu.agh.sarna.utils.kotlin.async

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import pl.edu.agh.sarna.R
import java.lang.ref.WeakReference

abstract class MethodAsyncTask(
        protected val contextReference: WeakReference<Context>,
        protected val response: AsyncResponse,
        protected val processID: Long,
        protected val serverState: Boolean,
        private val order : Int = 0) : AsyncTask<Void, Void, Int>() {

    private val progressDialog = ProgressDialog(contextReference.get())
    override fun onPreExecute() {
        progressDialog.setMessage(contextReference.get()!!.getString(R.string.loading))
        progressDialog.isIndeterminate = false
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(true)
        progressDialog.show()
    }
    abstract override fun doInBackground(vararg p0: Void?): Int
    override fun onPostExecute(result: Int?) {
        progressDialog.dismiss()
        when(order){
            1 -> response.onFirstFinished(result!!)
            else ->
                response.processFinish(result!!)
        }
    }
}