package pl.edu.agh.sarna.report

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.TextView
import com.github.mikephil.charting.charts.PieChart
import kotlinx.android.synthetic.main.activity_extended_report.*
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.dirtycow.task.DirtyCowReportTask
import pl.edu.agh.sarna.metadata.asynctask.MetadataReportTask
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.smsToken.task.TokenReportTask
import pl.edu.agh.sarna.utils.kotlin.PieDrawer
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.wifiPasswords.asynctask.WifiPasswordsReportTask
import java.lang.ref.WeakReference

class ExtendedReportActivity : AppCompatActivity(), AsyncResponse {
    @SuppressLint("SetTextI18n")
    override fun processFinish(output: Any) {
        val reportEntry = output as List<ReportEntry>
        for(entry in reportEntry){
            generateGraph(entry)
            generateDescription(entry)
        }

        titleTextView.text = methodTitle

    }

    private fun generateGraph(entry: ReportEntry) {
        if (entry.graphList != null){
            val title = TextView(this)
            title.gravity = Gravity.CENTER_HORIZONTAL
            title.setTextAppearance(this, R.style.TextAppearance_AppCompat_Medium)
            title.text = entry.graphList.title
            linear.addView(title)

            val pieChart = PieChart(this)
            val pie = PieDrawer(pieChart)
            pie.setData(entry.graphList.data)
            pie.generate()
            linear.addView(pieChart)
        }
    }

    private fun generateDescription(entry: ReportEntry) {
        if (entry.description != "") {
            val description = TextView(this)
            linear.addView(description)
            description.text = "${entry.description} \n"
            description.gravity = Gravity.CENTER_HORIZONTAL
        }
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
            getString(R.string.token_mode_not_safe) -> generateExtendTokenReport(runID, Mode.NOT_SAFE)
            getString(R.string.token_mode_for_dummies) -> generateExtendTokenReport(runID, Mode.DUMMY)
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
