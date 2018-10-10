package pl.edu.agh.sarna

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.model.Processes
import java.io.DataOutputStream
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {
    var rootAllowed:Boolean = false
    var educationalMode:Boolean = false
    var reportMode:Boolean = false
    var serverMode:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootSwitch.setOnCheckedChangeListener { buttonView, isChecked ->

            rootAllowed = if (isChecked){
                try {
                    val p = Runtime.getRuntime().exec("su")
                    val os = DataOutputStream(p.outputStream)
                    os.writeBytes("exit\n")
                    os.flush()

                    p.waitFor()
                    p.exitValue() == 0
                } catch (e: IOException){
                    showDeclineAlert(R.string.unrooted_device)
                    rootSwitch.isChecked = false
                    false
                }

            } else {
                false
            }
        };
    }

    fun onStartButtonClicked(view : View){
        educationalMode = eduSwitch.isChecked
        reportMode = reportSwitch.isChecked
        serverMode = serverSwitch.isChecked

        val processID  = launchDatabaseConnection()

        startActivity(Intent(this, WifiPasswordActivity::class.java).apply {
            putExtra("root_state", rootAllowed)
            putExtra("edu_state", educationalMode)
            putExtra("report_state", reportMode)
            putExtra("server_state", serverMode)
            putExtra("process_id", processID)
        })
    }

    private fun launchDatabaseConnection() : Long? {
        val dbHelper = DbHelper.getInstance(this)
        val db = dbHelper!!.writableDatabase

        val values = ContentValues().apply {
            put(Processes.ProcessEntry.COLUMN_NAME_START_TIME, Calendar.getInstance().timeInMillis.toString())
            put(Processes.ProcessEntry.COLUMN_NAME_SYSTEM_VERSION, Build.VERSION.SDK_INT)
            put(Processes.ProcessEntry.COLUMN_NAME_EDUCATIONAL, if (educationalMode) 1 else 0)
            put(Processes.ProcessEntry.COLUMN_NAME_REPORT, if (reportMode) 1 else 0)
            put(Processes.ProcessEntry.COLUMN_NAME_EXTERNAL_SERVER, if (serverMode) 1 else 0)
            put(Processes.ProcessEntry.COLUMN_NAME_ROOT_ALLOWED, if (rootAllowed) 1 else 0)
        }

        val processID = db?.insert(Processes.ProcessEntry.TABLE_NAME, null, values)
        Log.i("ID", "New process ID = $processID")

        return processID
    }

    private fun showDeclineAlert(description: Int) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(R.string.root_denied)
        dialogBuilder.setMessage(description)
        dialogBuilder.setPositiveButton("OK") { _, _ ->
            Log.i("FAILED_ROOT", "Cannot get root, not rooted device")
        }
        dialogBuilder.create().show()
    }

}
