package pl.edu.agh.sarna.report

import android.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_extended_report.*
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.dirtycow.task.DirtyCowReportTask
import pl.edu.agh.sarna.metadata.asynctask.MetadataReportTask
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.smsToken.task.TokenReportTask
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.wifiPasswords.asynctask.WifiPasswordsReportTask
import java.lang.ref.WeakReference

class ExtendedReportActivity : AppCompatActivity(), AsyncResponse {
    override fun processFinish(output: Any) {
        val info = StringBuilder()
        for(subtask in output as ArrayList<SubtaskStatus>){
            info.append("${subtask.description} : ${subtask.value}\n")
        }
        descriptionTextView.text = info
        titleTextView.text = methodTitle
    }

    private var rootState: Boolean = false
    private var eduState: Boolean = false
    private var serverState: Boolean = false
    private var reportState: Boolean = false
    private var processID: Long = 0
    private var runID: Long = 0
    private var methodTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extended_report)
        initialiseOptions()
        getData()
    }

    private fun getData() {
        when(methodTitle) {
            getString(R.string.dirtycow_title) -> generateExtendedDirtycowReport(runID)
            getString(R.string.wifi_title) -> generateExtendedWifiReport(runID)
            getString(R.string.metadata_title) -> generateExtendMetadataReport(runID)
            Mode.NOT_SAFE.description -> generateExtendTokenReport(runID, Mode.NOT_SAFE)
            Mode.DUMMY.description -> generateExtendTokenReport(runID, Mode.DUMMY)
        }
    }


    private fun generateExtendedDirtycowReport(runID: Long) {
        DirtyCowReportTask(WeakReference(this), this, runID).execute()
    }

    private fun generateExtendTokenReport(runID: Long, mode : Mode) {
        TokenReportTask(WeakReference(this), this, runID, mode).execute()
    }

    private fun generateExtendMetadataReport(runID: Long) {
        MetadataReportTask(WeakReference(this), this, runID).execute()
    }

    private fun generateExtendedWifiReport(runID: Long) {
        WifiPasswordsReportTask(WeakReference(this), this, runID).execute()
    }

    private fun initialiseOptions() {
        rootState = intent.getBooleanExtra("root_state", false)
        eduState = intent.getBooleanExtra("edu_state", false)
        serverState = intent.getBooleanExtra("server_state", false)
        reportState = intent.getBooleanExtra("report_state", false)
        processID = intent.getLongExtra("process_id", 0)
        runID = intent.getLongExtra("run_id", 0)
        methodTitle = intent.getStringExtra("title")
    }
}
