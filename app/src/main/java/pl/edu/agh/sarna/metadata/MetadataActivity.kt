package pl.edu.agh.sarna.metadata

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.metadata.asynctask.MetadataTask
import pl.edu.agh.sarna.permissions.checkCallLogsPermission
import pl.edu.agh.sarna.permissions.checkContactsPermission
import pl.edu.agh.sarna.report.ReportActivity
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse


class MetadataActivity : AppCompatActivity(), AsyncResponse {
    private var rootState: Boolean = false
    private var eduState: Boolean = false
    private var serverState: Boolean = false
    private var reportState: Boolean = false
    private var processID: Long = 0

    private fun initialiseOptions() {
        rootState = intent.getBooleanExtra("root_state", false)
        eduState = intent.getBooleanExtra("edu_state", false)
        serverState = intent.getBooleanExtra("server_state", false)
        reportState = intent.getBooleanExtra("report_state", false)
        processID = intent.getLongExtra("process_id", 0)
    }

    private var callLogPermissionGranted: Boolean = true
    private var contactPermissionGranted: Boolean = true
    private var permissionsGranted: Boolean = true

    override fun processFinish(output: Any) {
        if (output as Int == 0){
            runReport()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialiseOptions();
        setContentView(R.layout.activity_metadata)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun startMetadataTaking(view: View){
        if(!checkCallLogsPermission(this) and !checkContactsPermission(this)){
            requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS), 1)
        }
        else if (!checkCallLogsPermission(this)) {
            contactPermissionGranted = true
            requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG), 2)
        }
        else if (!checkContactsPermission(this)){
            callLogPermissionGranted = true
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 3)
        }
        else {
            permissionsGranted = true
            doJob()
        }

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                callLogPermissionGranted = checkCallLogsPermission(this)
                contactPermissionGranted = checkContactsPermission(this)
            }
            2 -> callLogPermissionGranted = checkCallLogsPermission(this)
            3 -> contactPermissionGranted = checkContactsPermission(this)
        }
        permissionsGranted = callLogPermissionGranted and contactPermissionGranted
        doJob()

    }

    private fun doJob() {
        MetadataTask(this, this, processID, callLogPermissionGranted, contactPermissionGranted, serverState).execute()
    }

    private fun runReport() {
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

