package pl.edu.agh.sarna.dirtycow.task

import android.content.Context
import android.util.Log
import pl.edu.agh.sarna.db.scripts.insertDirtyCowInfo
import pl.edu.agh.sarna.db.scripts.insertDirtyCowQuery
import pl.edu.agh.sarna.db.scripts.updateDirtyCowMethod
import pl.edu.agh.sarna.dirtycow.SystemInfo
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.async.MethodAsyncTask
import java.lang.ref.WeakReference

class DirtyCowTask(contextReference: WeakReference<Context>, response: AsyncResponse, processID: Long, serverState: Boolean)
    : MethodAsyncTask(contextReference, response, processID, serverState) {
    override fun doInBackground(vararg p0: Void?): Int {
        val runID = insertDirtyCowQuery(contextReference.get(), processID)
        val systemInfo = SystemInfo()
        systemInfo.launch()

        Log.i("DIRTYCOW", "Build ${systemInfo.buildDate}")
        Log.i("DIRTYCOW", "Kernel ${systemInfo.kernelVersion}")
        Log.i("DIRTYCOW", "Selinux ${systemInfo.isSELinuxInstalled}")
        Log.i("DIRTYCOW", "Vendor ${systemInfo.vendor}")

        System.loadLibrary("native-lib")
        val s = dcow()

        insertDirtyCowInfo(contextReference.get(),
                runID!!,
                1000,
                systemInfo.vendor,
                systemInfo.buildDate,
                systemInfo.isSELinuxInstalled,
                systemInfo.kernelVersion
        )

        if (s == "success") {
            updateDirtyCowMethod(contextReference.get(), runID, true)
            return 1
        }
        updateDirtyCowMethod(contextReference.get(), runID, false)
        return 0
    }
    private external fun dcow(): String
}