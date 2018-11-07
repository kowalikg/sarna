package pl.edu.agh.sarna

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import pl.edu.agh.sarna.db.scripts.launchDatabaseConnection
import pl.edu.agh.sarna.smsToken.TokenSms
import pl.edu.agh.sarna.wifiPasswords.WifiPasswordActivity
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

        //Required to load native shared library
        System.loadLibrary("native-lib");

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
                dialogBuilder.setTitle(getString(R.string.warning))
                        .setMessage(R.string.external_server_warning)
                        .setPositiveButton(getString(R.string.ok)) { _, _ ->
                            serverMode = true
                        }
                        .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                            serverSwitch.isChecked = false
                        }
                dialogBuilder.create().show()
            }
        }

        dcowSwitch.setOnCheckedChangeListener{_, isChecked ->
            if (isChecked) {
                var s = dcow();
                if (s.equals("success")) {
                    dcowTextView.setTextColor(Color.RED)
                    dcowTextView.text = "DirtyCOW: vulnerable"
                    dcowSwitch.visibility = View.INVISIBLE
                }
                else {
                    dcowTextView.text = "DirtyCOW: NOT vulnerable"
                }
            }
        };
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
            startActivity(Intent(this, TokenSms::class.java).apply {
                putExtra("root_state", rootAllowed)
                putExtra("edu_state", educationalMode)
                putExtra("report_state", reportMode)
                putExtra("server_state", serverMode)
                putExtra("process_id", processID)
            })
            overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
        }
    }



    private fun showDeclineAlert(description: Int) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(R.string.root_denied)
        dialogBuilder.setMessage(description)
        dialogBuilder.setPositiveButton(getString(R.string.ok)) { _, _ ->
            Log.i("FAILED_ROOT", "Cannot get root, not rooted device")
        }
        dialogBuilder.create().show()
    }

    private external fun dcow(): String

}
