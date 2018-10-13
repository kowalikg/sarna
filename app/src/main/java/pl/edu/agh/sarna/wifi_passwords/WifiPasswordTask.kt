package pl.edu.agh.sarna.wifi_passwords

import android.app.Activity
import android.os.AsyncTask
import android.app.ProgressDialog
import android.content.Context
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.util.Log
import pl.edu.agh.sarna.db.DbScripts
import pl.edu.agh.sarna.root.tools.execCommand
import pl.edu.agh.sarna.utils.java.WPAParser
import pl.edu.agh.sarna.utils.java.XMLParser
import pl.edu.agh.sarna.utils.kotlin.isOreo8_0
import pl.edu.agh.sarna.values.WifiLogsValues
import java.io.FileInputStream
import java.util.ArrayList


class WifiPasswordTask(val context: Activity, private val response: AsyncResponse, val processID: Long, private var permissionsGranted: Boolean = false, private var locationPermissionGranted: Boolean = false, private var storagePermissionGranted: Boolean = false) : AsyncTask<Void, Void, Int>() {
    private val progDailog = ProgressDialog(context)
    private var runID: Long = 0

    private var connected = false
    private var wifiSSID: String = ""
    private var passwordFound = false
    private var passwordContent = ""

    private val logsValues = WifiLogsValues()
    private val wifisAccessed: ArrayList<String> = ArrayList()


    override fun onPreExecute() {
        progDailog.setMessage("Loading...")
        progDailog.isIndeterminate = false
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progDailog.setCancelable(true)
        progDailog.show()
    }

    override fun doInBackground(vararg params: Void?): Int? {
        runID = DbScripts.insertWifiQuery(context, processID)!!
        requestWifiSsid()

        if (isOreo8_0()) {
            takePasswordFromXMLFile()
        } else takePasswordFromWPAFile()

        updateDatabase();
        Thread.sleep(1000)
        return 0
    }

    override fun onPostExecute(result: Int?) {
        progDailog.dismiss();
        response.processFinish(result!!)

    }

    private fun updateDatabase() {
        DbScripts.updateProcess(context, processID);
        DbScripts.updateWifiMethod(context, processID, passwordFound);
        DbScripts.insertWifiUtilsQuery(context.applicationContext, runID, storagePermissionGranted, locationPermissionGranted,
                connected, passwordFound, wifiSSID, passwordContent);
    }
    private fun requestWifiSsid() {
        val manager = this.context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (manager.isWifiEnabled) {
            val wifiInfo = manager.connectionInfo
            if (wifiInfo != null) {
                val state = WifiInfo.getDetailedStateOf(wifiInfo.supplicantState)
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    connected = true
                    wifiSSID = wifiInfo.ssid
                    wifiSSID = wifiSSID.replace("\"", "")
                }
            }
        }
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
}