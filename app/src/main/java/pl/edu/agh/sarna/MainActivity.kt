package pl.edu.agh.sarna

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import pl.edu.agh.sarna.cloak_and_dagger.CloakAndDaggerActivity
import pl.edu.agh.sarna.db.scripts.launchDatabaseConnection
import pl.edu.agh.sarna.dirtycow.DirtyCowActivity
import pl.edu.agh.sarna.wifiPasswords.WifiPasswordActivity
import java.io.DataOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private var rootAllowed: Boolean = false
    private var educationalMode: Boolean = false
    private var reportMode: Boolean = false
    private var serverMode: Boolean = false

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
        }
        serverSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
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

    }

    fun onStartButtonClicked(view: View) {
        educationalMode = eduSwitch.isChecked
        reportMode = reportSwitch.isChecked
        serverMode = serverSwitch.isChecked

        launchDatabaseConnection(this, educationalMode, reportMode, serverMode, rootAllowed)

        if (rootAllowed) {
            startActivity(Intent(this, WifiPasswordActivity::class.java))
            overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
        }
        else {
            startActivity(Intent(this, WifiPasswordActivity::class.java))
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


}
