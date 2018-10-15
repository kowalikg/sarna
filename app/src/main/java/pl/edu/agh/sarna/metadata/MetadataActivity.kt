package pl.edu.agh.sarna.metadata

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import pl.edu.agh.sarna.R
import android.provider.CallLog
import android.support.annotation.RequiresApi
import android.util.Log
import pl.edu.agh.sarna.permissions.checkCallLogsPermission
import pl.edu.agh.sarna.permissions.checkContactsPermission
import android.widget.Toast
import android.provider.ContactsContract
import pl.edu.agh.sarna.model.AsyncResponse
import pl.edu.agh.sarna.report.ReportActivity


class MetadataActivity : AppCompatActivity(), AsyncResponse {
    override fun processFinish(output: Any) {
        if (output as TaskStatus in arrayOf(TaskStatus.CALL_OK, TaskStatus.CALL_ERROR)) callProceed = true
        if (output in arrayOf(TaskStatus.CONTACTS_OK, TaskStatus.CONTACTS_ERROR)) contactsProceed = true

        if (callProceed and contactsProceed)
            startActivity(Intent(this, ReportActivity::class.java).apply {
                putExtra("root_state", rootState)
                putExtra("edu_state", eduState)
                putExtra("report_state", reportState)
                putExtra("server_state", serverState)
                putExtra("process_id", processID)
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialiseOptions();
        setContentView(R.layout.activity_metadata)
    }

    private var rootState: Boolean = false
    private var eduState: Boolean = false
    private var serverState: Boolean = false
    private var reportState: Boolean = false
    private var processID: Long = 0

    private var callProceed = false
    private var contactsProceed = false

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
        if (callLogPermissionGranted) doCallLogsJob()
        if (contactPermissionGranted) doContactsJob()

    }

    private fun doContactsJob() {
        ContactsTask(this, this, processID, contactPermissionGranted).execute()
    }

    private fun doCallLogsJob() {
       CallLogsTask(this, this, processID, callLogPermissionGranted).execute()
    }

}

