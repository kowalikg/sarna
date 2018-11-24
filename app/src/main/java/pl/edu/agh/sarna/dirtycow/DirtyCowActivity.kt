package pl.edu.agh.sarna.dirtycow

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.scripts.getLastProcess
import pl.edu.agh.sarna.dirtycow.task.DirtyCowTask
import pl.edu.agh.sarna.report.ReportActivity
import pl.edu.agh.sarna.smsToken.DefaultSms
import pl.edu.agh.sarna.smsToken.TokenSms
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import java.lang.ref.WeakReference

class DirtyCowActivity : AppCompatActivity(), AsyncResponse {
    override fun processFinish(output: Any) {
        nextActivity()
    }
    private var serverState: Boolean = false
    private var processID: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dirty_cow)
        initialiseOptions()
    }
    fun startDirtyCow(view: View){
        DirtyCowTask(WeakReference(this), this, processID, serverState).execute()
    }
    private fun initialiseOptions() {
        val options = getLastProcess(this)
        processID = options[0] as Long
        serverState = options[2] as Boolean
    }
    private fun nextActivity() {
        startActivity(Intent(this, TokenSms::class.java))
    }
    override fun onBackPressed() {}
}
