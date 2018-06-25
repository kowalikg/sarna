package pl.edu.agh.sarna

import android.Manifest
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
import pl.edu.agh.sarna.utils.Input
import java.io.File
import java.io.FileInputStream
import javax.xml.parsers.DocumentBuilderFactory
import pl.edu.agh.sarna.model.NetworkEntry
import pl.edu.agh.sarna.utils.Parser


class WifiPasswordActivity : AppCompatActivity() {
    var rootState:Boolean = false
    var eduState:Boolean = false
    var serverState:Boolean = false
    var reportState:Boolean = false

    var wifiSSID:String = ""

    val wifiFileToNougat = "wpa_supplicant.conf"
    val wifiFileFromOreo = "WifiConfigStore.xml"
    val cmd = "cat /data/misc/wifi/"

    var passwordFound = false
    var passwordContent = ""
    var localizationAllowed = false
    var connected = false
    var fileName = wifiFileToNougat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_password)

        initialiseOptions()

    }
    fun startWifiPasswordTaking(view : View) {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1);
//            }
//            else {
//                requestWifiSsid()
//            }
//            fileName = wifiFileFromOreo
//            if (rootState) takePasswordFromXMLFile()
//
//
//        } else {
//            doPreOreoJob()
//        }
        requestWifiSsid()
        takePasswordFromXMLFile()
    }

    private fun takePasswordFromXMLFile() {
        val command = cmd + wifiFileFromOreo + "> /storage/emulated/0/bom.txt"
        val content = getCommandOutput(command)

        val `in` = FileInputStream("/storage/emulated/0/bom.txt")
        val parser = Parser()
        parser.parse(`in`)
        `in`.close()

        for (entry in parser.getParsedEntries()) {
           Log.i("WIFI", "SSID: " + entry.getSsid() + " Password: " + entry.getPassword())
            if (entry.ssid.equals(wifiSSID))
                Log.i("WIFI", "FOUND")
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
            1 -> {
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

        }// other 'case' lines to check for other
        localizationAllowed = true
        requestWifiSsid()

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
