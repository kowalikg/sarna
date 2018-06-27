package pl.edu.agh.sarna

import android.Manifest
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.NetworkInfo.DetailedState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import pl.edu.agh.sarna.utils.WPAParser
import pl.edu.agh.sarna.utils.XMLParser
import pl.edu.agh.sarna.values.PermissionCode
import pl.edu.agh.sarna.values.WifiLogsValues
import java.io.FileInputStream
import java.util.*
import java.util.stream.Collectors

class WifiPasswordActivity : AppCompatActivity() {
    private var rootState:Boolean = false
    private var eduState:Boolean = false
    private var serverState:Boolean = false
    private var reportState:Boolean = false

    private var wifiSSID:String = ""

    private val logsValues = WifiLogsValues()

    private var passwordFound = false
    private var passwordContent = ""
    private var localizationAllowed = false
    private var connected = false
    private var fileName = logsValues.wifiFileToNougat

    private var storageAllowed = false

    private val wifisAccessed:ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_password)

        initialiseOptions()

    }
    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun startWifiPasswordTaking(view : View) {

        requestInternalStoragePermission()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) doPostOreoJob()
        else doPreOreoJob()

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun doPreOreoJob() {
        requestWifiSsid()
        takePasswordFromWPAFile()
        if (reportState and storageAllowed) generateReport()
    }

    private fun takePasswordFromWPAFile() {
        if (!storageAllowed) return

        val command = "${logsValues.cmd}${logsValues.wifiFileToNougat}>${logsValues.logFile}"
        execCommand(command)

        val parser = WPAParser(logsValues.logFile)
        val parsedEntries = parser.parse()

        wifisAccessed.clear()

        for (entry in parsedEntries) {
            wifisAccessed.add(entry.ssid)

            if (entry.ssid == wifiSSID){
                passwordFound = true
                passwordContent =
                        if (entry.password.isEmpty()) "no password"
                        else entry.password
            }

        }
    }

    private fun takePasswordFromXMLFile() {
        if (!storageAllowed) return

        val command = "${logsValues.cmd}${logsValues.wifiFileFromOreo}>${logsValues.logFile}"
        execCommand(command)

        val `in` = FileInputStream(logsValues.logFile)
        val parser = XMLParser()
        parser.parse(`in`)
        `in`.close()

        wifisAccessed.clear()
        for (entry in parser.parsedEntries) {
            wifisAccessed.add(entry.ssid)

            if (entry.ssid == wifiSSID){
                passwordFound = true
                passwordContent =
                        if (entry.password.isEmpty()) "no password"
                        else entry.password
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun doPostOreoJob() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            requestLocationPermission()
        }
        requestWifiSsid()
        fileName = logsValues.wifiFileFromOreo
        if (rootState) takePasswordFromXMLFile()
        if ((android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1)
                and localizationAllowed and reportState and storageAllowed) generateReport()
        else if (reportState and storageAllowed) generateReport()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PermissionCode.LOCATION.ordinal);
        }
        else {
            localizationAllowed = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun generateReport() {
        val builder = AlertDialog.Builder(this)
        val title = "WIFI Report"

        val message = StringBuilder()

        message.append("Android version: ")
                .append(android.os.Build.VERSION.RELEASE)
                .append("\n")

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1)
            message.append("Location allowed: ").append(localizationAllowed).append("\n")

        message.append("Analysed file: ").append(fileName).append("\n")
        message.append("Wifi connected: ").append(connected).append("\n")
        if (connected){
            message.append("Wifi ssid: ").append(wifiSSID).append("\n")
            message.append("Password found: ").append(passwordFound).append("\n")
            if (passwordFound) message.append("Password: ").append(passwordContent).append("\n")

        }

        if (!wifisAccessed.isEmpty())
            message.append("Network password accessed: ")
                    .append(wifisAccessed.stream()
                    .collect(Collectors.joining("\n")))

        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK", { _, _ ->
            Log.i("REPORT", "Finished")
        })
        val dialog = builder.create()
        dialog.show()


    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun requestInternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PermissionCode.STORAGE.ordinal);
        }
        else {
            storageAllowed = true
        }
    }



    private fun execCommand(command: String) {
        val process = Runtime.getRuntime().exec("su")
        val out = process.outputStream

        out.write(command.toByteArray())
        out.flush()
        out.close()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PermissionCode.LOCATION.ordinal -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    localizationAllowed = true
                }
                return
            }
            PermissionCode.STORAGE.ordinal -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    storageAllowed = true
                }
                return
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


    private fun initialiseOptions() {
        rootState = intent.getBooleanExtra("root_state", false)
        eduState = intent.getBooleanExtra("edu_state", false)
        serverState = intent.getBooleanExtra("server_state", false)
        reportState = intent.getBooleanExtra("report_state", false)
    }



}
