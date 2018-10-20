package pl.edu.agh.sarna

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import pl.edu.agh.sarna.db.scripts.launchDatabaseConnection
import pl.edu.agh.sarna.metadata.MetadataActivity
import pl.edu.agh.sarna.wifi_passwords.WifiPasswordActivity
import java.io.DataOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    var rootAllowed: Boolean = false
    var educationalMode: Boolean = false
    var reportMode: Boolean = false
    var serverMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootSwitch.setOnCheckedChangeListener { _, isChecked ->

            rootAllowed = if (isChecked) {
                try {
                    val p = Runtime.getRuntime().exec("su")
                    val os = DataOutputStream(p.outputStream)
                    os.writeBytes("exit\n")
                    os.flush()

                    p.waitFor()
                    p.exitValue() == 0
                } catch (e: IOException) {
                    showDeclineAlert(R.string.unrooted_device)
                    rootSwitch.isChecked = false
                    false
                }

            } else {
                false
            }
        };
        serverSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("Warning!")
                        .setMessage(R.string.external_server_warning)
                        .setPositiveButton("OK") { _, _ ->
                            serverMode = true
                        }
                        .setNegativeButton("Cancel") { _, _ ->
                            serverSwitch.isChecked = false
                        }
                dialogBuilder.create().show()
            }

        }
    }

    fun onStartButtonClicked(view: View) {
        educationalMode = eduSwitch.isChecked
        reportMode = reportSwitch.isChecked
        serverMode = serverSwitch.isChecked

        val processID = launchDatabaseConnection(this, educationalMode, reportMode, serverMode, rootAllowed)

        if (rootAllowed) startActivity(Intent(this, WifiPasswordActivity::class.java).apply {
            putExtra("root_state", rootAllowed)
            putExtra("edu_state", educationalMode)
            putExtra("report_state", reportMode)
            putExtra("server_state", serverMode)
            putExtra("process_id", processID)
        })
        else {
            startActivity(Intent(this, MetadataActivity::class.java).apply {
                putExtra("root_state", rootAllowed)
                putExtra("edu_state", educationalMode)
                putExtra("report_state", reportMode)
                putExtra("server_state", serverMode)
                putExtra("process_id", processID)
            })
        }
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
