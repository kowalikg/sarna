package pl.edu.agh.sarna.wifiPasswords.asynctask

import android.content.Context
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.support.annotation.RequiresApi
import pl.edu.agh.sarna.db.scripts.insertWifiQuery
import pl.edu.agh.sarna.db.scripts.insertWifiUtilsQuery
import pl.edu.agh.sarna.db.scripts.updateWifiMethod
import pl.edu.agh.sarna.root.tools.execCommand
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.async.MethodAsyncTask
import pl.edu.agh.sarna.utils.kotlin.isOreo8_0
import pl.edu.agh.sarna.wifiPasswords.parsers.WPAParser
import pl.edu.agh.sarna.wifiPasswords.parsers.XMLParser
import pl.edu.agh.sarna.wifiPasswords.values.WifiLogsValues
import java.io.FileInputStream
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.locks.ReentrantLock


class WifiPasswordTask(contextReference: WeakReference<Context>, response: AsyncResponse, processID: Long,
                       serverState: Boolean,
                       private val rootState: Boolean,
                       private var permissionsGranted: Boolean = false,
                       private var locationPermissionGranted: Boolean = false,
                       private var storagePermissionGranted: Boolean = false)
    : MethodAsyncTask(contextReference, response, processID, serverState) {
    private var runID: Long = 0

    private var connected = false
    private var wifiSSID: String = "-"
    private var passwordFound = false
    private var passwordContent = "-"

    private val lock = ReentrantLock()

    private val logsValues = WifiLogsValues()
    private val wifisAccessed: ArrayList<String> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun doInBackground(vararg p0: Void?): Int {
        runID = insertWifiQuery(contextReference.get()!!, processID)!!
        requestWifiSsid()

        if (rootState and permissionsGranted){
            if (isOreo8_0()) {
                takePasswordFromXMLFile()
            } else takePasswordFromWPAFile()
        }

        updateDatabase();
        Thread.sleep(1000)
        return 0
    }

    private fun updateDatabase() {
        lock.lock()
        insertWifiUtilsQuery(contextReference.get()!!, runID, storagePermissionGranted, locationPermissionGranted,
                connected, passwordFound, wifiSSID, passwordContent)
        updateWifiMethod(contextReference.get()!!, processID, passwordFound)
        lock.unlock()
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestWifiSsid() {
        val manager = contextReference.get()!!.getSystemService(Context.WIFI_SERVICE) as WifiManager
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