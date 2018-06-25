package pl.edu.agh.sarna

import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.CompoundButton
import java.io.DataOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    var rootAllowed:Boolean = false
    var educationalMode:Boolean = false
    var reportMode:Boolean = false
    var serverMode:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Example of a call to a native method
        //sample_text.text = stringFromJNI()
        rootSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            // do something, the isChecked will be
            // true if the switch is in the On position

            Log.i("DEBUG","rootswitch is " + isChecked.toString())
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

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    fun onStartButtonClicked(view : View){
        Log.i("DEBUG","CLICKED")
        educationalMode = eduSwitch.isChecked
        reportMode = reportSwitch.isChecked
        serverMode = serverSwitch.isChecked
        startActivity(Intent(this, WifiPasswordActivity::class.java).apply {
            putExtra("root_state", rootAllowed)
            putExtra("edu_state", educationalMode)
            putExtra("report_state", reportMode)
            putExtra("server_state", serverMode)
        })
    }

    private fun showDeclineAlert(description: Int) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(R.string.root_denied)
        dialogBuilder.setMessage(description)
        dialogBuilder.setPositiveButton("OK", { _, _ ->
            Log.i("FAILED_ROOT", "Cannot get root, not rooted device")
        })
        dialogBuilder.create().show()
    }

}