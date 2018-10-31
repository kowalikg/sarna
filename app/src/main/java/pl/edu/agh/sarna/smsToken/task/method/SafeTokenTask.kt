package pl.edu.agh.sarna.smsToken.task.method

import android.content.Context
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.async.MethodAsyncTask
import java.lang.ref.WeakReference

class SafeTokenTask(contextReference: WeakReference<Context>,
                    response: AsyncResponse,
                    processID: Long) : MethodAsyncTask(contextReference, response, processID, 1) {
    override fun doInBackground(vararg p0: Void?): Int {
        return 0
    }

}