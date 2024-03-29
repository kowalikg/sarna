package pl.edu.agh.sarna.metadata

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.scripts.getLastProcess
import pl.edu.agh.sarna.metadata.asynctask.MetadataTaskMethod
import pl.edu.agh.sarna.permissions.checkCallLogsPermission
import pl.edu.agh.sarna.permissions.checkContactsPermission
import pl.edu.agh.sarna.report.ReportActivity
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.isKitKat4_4
import java.lang.ref.WeakReference


class MetadataActivity : AppCompatActivity(), AsyncResponse {
    private var serverState: Boolean = false
    private var processID: Long = 0

    private fun initialiseOptions() {
        val options = getLastProcess(this)
        processID = options[0] as Long
        serverState = options[2] as Boolean
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
        if (isKitKat4_4()){
            if(!checkCallLogsPermission(this) and !checkContactsPermission(this)){
                requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS), 1)
            }
            else if (!checkCallLogsPermission(this)) {
                contactPermissionGranted = true
                //requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG), 2)
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
        MetadataTaskMethod(WeakReference(this), this, processID, serverState, callLogPermissionGranted, contactPermissionGranted).execute()
    }

    @SuppressLint("PrivateResource")
    private fun runReport() {
        startActivity(Intent(this, ReportActivity::class.java))

        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
    }

    override fun onBackPressed() {}
}

