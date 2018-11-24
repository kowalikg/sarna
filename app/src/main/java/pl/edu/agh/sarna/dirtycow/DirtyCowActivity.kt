package pl.edu.agh.sarna.dirtycow

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import pl.edu.agh.sarna.R
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
    private var rootState: Boolean = false
    private var eduState: Boolean = false
    private var serverState: Boolean = false
    private var reportState: Boolean = false
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
        rootState = intent.getBooleanExtra("root_state", false)
        eduState = intent.getBooleanExtra("edu_state", false)
        serverState = intent.getBooleanExtra("server_state", false)
        reportState = intent.getBooleanExtra("report_state", false)
        processID = intent.getLongExtra("process_id", 0)
    }
    private fun nextActivity() {
        startActivity(Intent(this, ReportActivity::class.java).apply {
            putExtra("root_state", rootState)
            putExtra("edu_state", eduState)
            putExtra("report_state", reportState)
            putExtra("server_state", serverState)
            putExtra("process_id", processID)
        })
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
    }
    override fun onBackPressed() {}
}
