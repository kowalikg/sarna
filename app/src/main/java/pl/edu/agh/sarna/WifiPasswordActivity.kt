package pl.edu.agh.sarna

import android.Manifest
import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.NetworkInfo.DetailedState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.BaseColumns
import android.support.annotation.NonNull
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.Process
import pl.edu.agh.sarna.utils.WPAParser
import pl.edu.agh.sarna.utils.XMLParser
import pl.edu.agh.sarna.values.WifiLogsValues
import java.io.FileInputStream
import java.util.*


class WifiPasswordActivity : AppCompatActivity() {
    private var rootState:Boolean = false
    private var eduState:Boolean = false
    private var serverState:Boolean = false
    private var reportState:Boolean = false
    private var processID:Long = 0

    private var wifiSSID:String = ""

    private val logsValues = WifiLogsValues()

    private var passwordFound = false
    private var passwordContent = ""
    private var connected = false
    private var fileName = logsValues.wifiFileToNougat

    private var permissionsGranted = false;

    private val wifisAccessed:ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_password)

        initialiseOptions()

    }
    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun startWifiPasswordTaking(view : View) {
        if (!checkPermissions())
            requestSelectedPermissions()
        else
            doJob()


    }
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun checkPermissions() : Boolean {
        var granted: Boolean
        granted = this.checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1)
            granted = granted and (this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        return granted
    }

    private fun doJob() {
        requestWifiSsid()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            takePasswordFromXMLFile()
        }
        else takePasswordFromWPAFile()
        updateProcess()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestSelectedPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION), 10)
        }
        else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 20)
        }

    }

    private fun updateProcess() {
        val db = DbHelper.getInstance(this)

        val cv = ContentValues()
        cv.put(Process.ProcessEntry.COLUMN_NAME_END_TIME, Calendar.getInstance().timeInMillis.toString())
        db!!.writableDatabase.update(Process.ProcessEntry.TABLE_NAME, cv, "_id = ?", arrayOf(processID.toString()));

        val dbReadable = db.readableDatabase

        val projection = arrayOf(
                BaseColumns._ID,
                Process.ProcessEntry.COLUMN_NAME_START_TIME,
                Process.ProcessEntry.COLUMN_NAME_END_TIME,
                Process.ProcessEntry.COLUMN_NAME_SYSTEM_VERSION,
                Process.ProcessEntry.COLUMN_NAME_ROOT_ALLOWED
        )


        val selection = "_id = ?"
        val selectionArgs = arrayOf(processID.toString())

        val cursor = dbReadable.query(
                Process.ProcessEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
               null               // The sort order
        )

        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val start = getString(getColumnIndexOrThrow(Process.ProcessEntry.COLUMN_NAME_START_TIME))
                val end = getString(getColumnIndexOrThrow(Process.ProcessEntry.COLUMN_NAME_END_TIME))
                val system = getFloat(getColumnIndexOrThrow(Process.ProcessEntry.COLUMN_NAME_SYSTEM_VERSION))
                val root = getInt(getColumnIndexOrThrow(Process.ProcessEntry.COLUMN_NAME_ROOT_ALLOWED))
                Log.i("ID: ", "$itemId $start $end $system $root $wifiSSID" )
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

            if (entry.ssid == wifiSSID){
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

            if (entry.ssid == wifiSSID){
                passwordFound = true
                passwordContent =
                        if (entry.password.isEmpty()) "no password"
                        else entry.password
            }

        }
    }

    private fun execCommand(command: String) {
        val process = Runtime.getRuntime().exec("su")
        val out = process.outputStream

        out.write(command.toByteArray())
        out.flush()
        out.close()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            10, 20 ->
            if (hasAllPermissionsGranted(grantResults)) {
                permissionsGranted = true
                doJob()

            } else {
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }
    private fun hasAllPermissionsGranted(@NonNull grantResults: IntArray) : Boolean {
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
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
        processID = intent.getLongExtra("process_id", 0)
    }



}
