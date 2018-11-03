package pl.edu.agh.sarna.wifi_passwords.asynctask

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.AsyncTask
import pl.edu.agh.sarna.db.scripts.*
import pl.edu.agh.sarna.root.tools.execCommand
import pl.edu.agh.sarna.utils.java.WPAParser
import pl.edu.agh.sarna.utils.java.XMLParser
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.isOreo8_0
import pl.edu.agh.sarna.values.WifiLogsValues
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.locks.ReentrantLock


class WifiPasswordTask(
        val context: Activity,
        private val response: AsyncResponse,
        val processID: Long,
        val rootState: Boolean,
        private var permissionsGranted: Boolean = false,
        private var locationPermissionGranted: Boolean = false,
        private var storagePermissionGranted: Boolean = false,
        val sendingDataToServerAllowed: Boolean
) : AsyncTask<Void, Void, Int>() {
    private val progDailog = ProgressDialog(context)
    private var runID: Long = 0

    private var connected = false
    private var wifiSSID: String = "-"
    private var passwordFound = false
    private var passwordContent = "-"

    private val lock = ReentrantLock()

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
        val startTime = Calendar.getInstance().timeInMillis
        runID = insertWifiQuery(context, processID, startTime)!!
        requestWifiSsid()

        if (rootState and permissionsGranted){
            if (isOreo8_0()) {
                takePasswordFromXMLFile()
            } else takePasswordFromWPAFile()
        }

        val endTime = Calendar.getInstance().timeInMillis
        updateDatabase(startTime, endTime)
        Thread.sleep(1000)
        return 0
    }

    override fun onPostExecute(result: Int?) {
        progDailog.dismiss()
        response.processFinish(result!!)

    }

    private fun updateDatabase(startTime: Long, endTime: Long) {
        lock.lock()
        insertWifiUtilsQuery(context.applicationContext, runID, storagePermissionGranted, locationPermissionGranted,
                connected, passwordFound, wifiSSID, passwordContent)
        updateWifiMethod(context.applicationContext, endTime, processID, passwordFound)
        lock.unlock()

        if (sendingDataToServerAllowed) {
            saveWifiUtilsToMongo(runID, storagePermissionGranted, locationPermissionGranted, connected, passwordFound,
                    wifiSSID, passwordContent)
            saveWifiPasswordsToMongo(processID, startTime, endTime, passwordFound)
        }
    }
    private fun requestWifiSsid() {
        val manager = this.context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (manager.isWifiEnabled) {
            val wifiInfo = manager.connectionInfo
            if (wifiInfo != null) {
                val state = WifiInfo.getDetailedStateOf(wifiInfo.supplicantState)
                if (state == NetworkInfo.DetailedState.CONNECTED
                        || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    connected = true
                    wifiSSID = wifiInfo.ssid
                    wifiSSID = wifiSSID.replace("\"", "")
                }
            }
        }
    }

    private fun takePasswordFromWPAFile() {
        lock.lock()
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
        lock.unlock()
    }

    private fun takePasswordFromXMLFile() {
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
