package pl.edu.agh.sarna.wifiPasswords

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.scripts.getLastProcess
import pl.edu.agh.sarna.dirtycow.DirtyCowActivity
import pl.edu.agh.sarna.permissions.checkLocationPermission
import pl.edu.agh.sarna.permissions.checkStoragePermission
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.isOreo8_1
import pl.edu.agh.sarna.wifiPasswords.asynctask.WifiPasswordTask
import java.lang.ref.WeakReference


class WifiPasswordActivity : AppCompatActivity(), AsyncResponse {
    private var rootState: Boolean = false
    private var eduState: Boolean = false
    private var serverState: Boolean = false
    private var processID: Long = 0

    private var permissionsGranted = false;
    private var locationPermissionGranted = false;
    private var storagePermissionGranted = false;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_password)

        initialiseOptions()
    }

    private fun initialiseOptions() {
        val options = getLastProcess(this)
        processID = options[0] as Long
        rootState = options[1] as Boolean
        serverState = options[2] as Boolean

    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun startWifiPasswordTaking(view: View) {
        if (!checkPermissions())
            requestSelectedPermissions()
        else {
            permissionsGranted = true
            doJob()
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun checkPermissions(): Boolean {
        storagePermissionGranted = checkStoragePermission(this)
        if (isOreo8_1()) {
            locationPermissionGranted = checkLocationPermission(this)
            return storagePermissionGranted and locationPermissionGranted
        }
        return storagePermissionGranted
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestSelectedPermissions() {
        if (isOreo8_1()) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION), 10)
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 20)
        }

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 10) {
            locationPermissionGranted = checkLocationPermission(this)
            storagePermissionGranted = checkStoragePermission(this)
            permissionsGranted = locationPermissionGranted and storagePermissionGranted
        }
        else if (requestCode == 20){
            storagePermissionGranted = checkStoragePermission(this)
            permissionsGranted = storagePermissionGranted

        }
        doJob()
    }

    private fun doJob() {
        WifiPasswordTask(WeakReference(this), this, processID, serverState, rootState, permissionsGranted, locationPermissionGranted, storagePermissionGranted).execute()
    }
    @SuppressLint("PrivateResource")
    override fun processFinish(output: Any) {
        startActivity(Intent(this, DirtyCowActivity::class.java))
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
    }

    override fun onBackPressed() {}


}
