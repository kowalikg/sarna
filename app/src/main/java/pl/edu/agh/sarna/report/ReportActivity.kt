package pl.edu.agh.sarna.report

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_report.*
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.dirtycow.task.DirtyCowReportTask
import pl.edu.agh.sarna.metadata.asynctask.MetadataReportTask
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.report.asynctask.DbReportTask
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.smsToken.task.TokenReportTask
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.wifiPasswords.asynctask.WifiPasswordsReportTask
import java.lang.ref.WeakReference


class ReportActivity : AppCompatActivity(), AsyncResponse {
    private var rootState: Boolean = false
    private var eduState: Boolean = false
    private var serverState: Boolean = false
    private var reportState: Boolean = false
    private var processID: Long = 0
    private val successDrawable = GradientDrawable()
    private val failDrawable = GradientDrawable()

    private val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        initialiseLayout()
        initialiseOptions()
        DbReportTask(WeakReference(this), this, processID, rootState).execute()

    }

    private fun extendedReportOnClickListener(runID: Long): View.OnClickListener? {
        return View.OnClickListener {view ->
            launchExtendedReport(runID, view.tag.toString())
        }
    }


    private fun initialiseLayout() {
        layoutParams.setMargins(0, 0, 0, 10)

        successDrawable.setColor(Color.rgb(0, 204, 102))
        successDrawable.cornerRadius = 10f
        successDrawable.setStroke(1, Color.WHITE)

        failDrawable.setColor(Color.rgb(255, 51, 51))
        failDrawable.cornerRadius = 10f
        failDrawable.setStroke(1, Color.WHITE)
    }

    override fun onBackPressed() {

    }
    private fun initialiseOptions() {
        rootState = intent.getBooleanExtra("root_state", false)
        eduState = intent.getBooleanExtra("edu_state", false)
        serverState = intent.getBooleanExtra("server_state", false)
        reportState = intent.getBooleanExtra("report_state", false)
        processID = intent.getLongExtra("process_id", 0)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun processFinish(output: Any) {
        for (subtask in output as ArrayList<SubtaskStatus>){
            val button = Button(this)
            button.tag = subtask.description
            button.text = subtask.description

            val runID = subtask._id
            if (reportState) button.setOnClickListener(extendedReportOnClickListener(runID))

            button.background = if (subtask.value as Boolean) successDrawable else failDrawable

            reportLayout.addView(button, layoutParams)
        }
    }
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun terminateApp(view : View){
        this.finishAffinity()
        System.exit(0)
    }
    private fun launchExtendedReport(runID: Long, methodTitle: String) {
        startActivity(Intent(this, ExtendedReportActivity::class.java).apply {
            putExtra("root_state", rootState)
            putExtra("edu_state", eduState)
            putExtra("report_state", reportState)
            putExtra("server_state", serverState)
            putExtra("process_id", processID)
            putExtra("run_id", runID)
            putExtra("title", methodTitle)
        })
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
    }
}
