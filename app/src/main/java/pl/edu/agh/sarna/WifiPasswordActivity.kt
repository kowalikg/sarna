package pl.edu.agh.sarna


import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.os.Build
import android.support.annotation.RequiresApi
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.NetworkInfo.DetailedState
import android.net.wifi.WifiInfo
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import pl.edu.agh.sarna.utils.Input
import java.io.File
import java.io.FileInputStream
import javax.xml.parsers.DocumentBuilderFactory
import pl.edu.agh.sarna.model.NetworkEntry
import pl.edu.agh.sarna.utils.Parser
import java.util.*
import java.util.stream.Collectors


class WifiPasswordActivity : AppCompatActivity() {
    var rootState:Boolean = false
    var eduState:Boolean = false
    var serverState:Boolean = false
    var reportState:Boolean = false

    var wifiSSID:String = ""

    val wifiFileToNougat = "wpa_supplicant.conf"
    val wifiFileFromOreo = "WifiConfigStore.xml"
    val cmd = "cat /data/misc/wifi/"

    val logFile = "/storage/emulated/0/bom.txt"

    var passwordFound = false
    var passwordContent = ""
    var localizationAllowed = false
    var connected = false
    var fileName = wifiFileToNougat

    val locationRequestCode = 1
    val storageRequestCode = 2

    var storageAllowed = false

    val wifisAccessed:ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_password)

        initialiseOptions()

    }
    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun startWifiPasswordTaking(view : View) {

        requestInternalStorageLocation()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationRequestCode);
                }
                else {
                    localizationAllowed = true
                    requestWifiSsid()
                }
            }
            else {
                requestWifiSsid()
            }
            fileName = wifiFileFromOreo
            if (rootState) takePasswordFromXMLFile()


        } else {
            doPreOreoJob()
        }
        generateReport()
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
    private fun requestInternalStorageLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), storageRequestCode);
        }
        else {
            storageAllowed = true
        }
    }

    private fun takePasswordFromXMLFile() {
        val command = "$cmd$wifiFileFromOreo>$logFile"
        val content = getCommandOutput(command)

        if (!storageAllowed) return

        val `in` = FileInputStream(logFile)
        val parser = Parser()
        parser.parse(`in`)
        `in`.close()

        for (entry in parser.getParsedEntries()) {
            wifisAccessed.add(entry.ssid)
            if (entry.ssid.equals(wifiSSID)){
                passwordFound = true
                passwordContent = if (entry.password.isEmpty()) "none"
                else entry.password
            }


        }


    }

    private fun getCommandOutput(command: String) : String {
        val process = Runtime.getRuntime().exec("su")
        val `in` = process.inputStream
        val out = process.outputStream


        out.write(command.toByteArray())
        out.flush()
        out.close()
        val buffer = ByteArray(1024*1000)
        val length = `in`.read(buffer)
        return ""
//        return String(buffer, 0, length)
    }


    private fun doPreOreoJob() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            locationRequestCode -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    localizationAllowed = true
                    requestWifiSsid()

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
            storageRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    storageAllowed = true

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

        }// other 'case' lines to check for other

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

    external fun getWifiPassword(): String

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }


}
