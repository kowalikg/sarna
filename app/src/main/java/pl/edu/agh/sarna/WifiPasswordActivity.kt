package pl.edu.agh.sarna

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.net.NetworkInfo.DetailedState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_wifi_password.*
import pl.edu.agh.sarna.db.DbScripts
import pl.edu.agh.sarna.permissions.checkLocationPermision
import pl.edu.agh.sarna.permissions.checkStoragePermission
import pl.edu.agh.sarna.root.tools.execCommand
import pl.edu.agh.sarna.utils.java.WPAParser
import pl.edu.agh.sarna.utils.java.XMLParser
import pl.edu.agh.sarna.utils.kotlin.isOreo8_0
import pl.edu.agh.sarna.utils.kotlin.isOreo8_1
import pl.edu.agh.sarna.values.WifiLogsValues
import java.io.FileInputStream
import java.util.*


class WifiPasswordActivity : AppCompatActivity() {
    private var rootState: Boolean = false
    private var eduState: Boolean = false
    private var serverState: Boolean = false
    private var reportState: Boolean = false
    private var processID: Long = 0
    private var runID: Long = 0

    private var wifiSSID: String = ""

    private val logsValues = WifiLogsValues()

    private var passwordFound = false
    private var passwordContent = ""
    private var connected = false

    private var permissionsGranted = false;
    private var locationPermissionGranted = false;
    private var storagePermissionGranted = false;

    private val wifisAccessed: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_password)

        initialiseOptions()
    }

    private fun initialiseOptions() {
        rootState = intent.getBooleanExtra("root_state", false)
        eduState = intent.getBooleanExtra("edu_state", false)
        serverState = intent.getBooleanExtra("server_state", false)
        reportState = intent.getBooleanExtra("report_state", false)
        processID = intent.getLongExtra("process_id", 0)
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun startWifiPasswordTaking(view: View) {
        wifiButton.isClickable = false
        runID = DbScripts.insertWifiQuery(this, processID)!!
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
            locationPermissionGranted = checkLocationPermision(this)
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
            locationPermissionGranted = checkLocationPermision(this)
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
        requestWifiSsid()

        if (isOreo8_0()) {
            takePasswordFromXMLFile()
        } else takePasswordFromWPAFile()

        updateDatabase();
    }

    private fun updateDatabase() {
        DbScripts.updateProcess(this, processID);
        DbScripts.updateWifiMethod(this, processID, passwordFound);
        DbScripts.insertWifiUtilsQuery(this, runID, storagePermissionGranted, locationPermissionGranted,
                connected, passwordFound, wifiSSID, passwordContent);
    }


    private fun takePasswordFromWPAFile() {
        if (!permissionsGranted) return

        val command = "${logsValues.cmd}${logsValues.wifiFileToNougat}>${logsValues.logFile}"
        execCommand(command)

        val parser = WPAParser(logsValues.logFile)
        val parsedEntries = parser.parse()

        wifisAccessed.clear()

        for (entry in parsedEntries) {
            wifisAccessed.add(entry.ssid)

            if (entry.ssid == wifiSSID) {
                passwordFound = true
                passwordContent =
                        if (entry.password.isEmpty()) "no password"
                        else entry.password
            }

        }
    }

    private fun takePasswordFromXMLFile() {
        if (!permissionsGranted) return

        val command = "${logsValues.cmd}${logsValues.wifiFileFromOreo}>${logsValues.logFile}"
        execCommand(command)

        val `in` = FileInputStream(logsValues.logFile)
        val parser = XMLParser()
        parser.parse(`in`)
        `in`.close()

        wifisAccessed.clear()
        for (entry in parser.parsedEntries) {
            wifisAccessed.add(entry.ssid)

            if (entry.ssid == wifiSSID) {
                passwordFound = true
                passwordContent =
                        if (entry.password.isEmpty()) "no password"
                        else entry.password
            }

        }
    }


    private fun requestWifiSsid() {
        val manager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (manager.isWifiEnabled) {
            val wifiInfo = manager.connectionInfo
            if (wifiInfo != null) {
                val state = WifiInfo.getDetailedStateOf(wifiInfo.supplicantState)
                if (state == DetailedState.CONNECTED || state == DetailedState.OBTAINING_IPADDR) {
                    connected = true
                    wifiSSID = wifiInfo.ssid
                    wifiSSID = wifiSSID.replace("\"", "")
                }
            }
        }
    }
}
