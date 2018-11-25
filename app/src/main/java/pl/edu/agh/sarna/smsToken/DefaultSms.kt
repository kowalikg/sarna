package pl.edu.agh.sarna.smsToken

import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Telephony
import android.support.annotation.RequiresApi
import android.view.View
import kotlinx.android.synthetic.main.activity_default_sms.*
import kotlinx.android.synthetic.main.activity_token_sms.*
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.scripts.*
import pl.edu.agh.sarna.metadata.MetadataActivity
import pl.edu.agh.sarna.report.ReportActivity
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.smsToken.task.method.DummyTask
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.isDefaultSmsApp
import java.lang.ref.WeakReference

class DefaultSms : AppCompatActivity(), AsyncResponse {
    private var serverState: Boolean = false
    private var processID: Long = 0

    private var phoneNumber = "+48731464100"

    private var mode = Mode.TEST_DUMMY
    private var runID: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default_sms)
        initialiseOptions()
    }
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun startDefault(view: View){
        if (isDefaultSmsApp(this)) {
            dummyTask()
        } else {
            requestDefaultApp()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        dummyTask()
    }

    private fun dummyTask(){
        runID = insertTokenQuery(this, processID, mode.ordinal)!!
        insertSmsPermissions(this, runID)
        if (!isDefaultSmsApp(this)){
            updateTokenMethod(this, runID, false)
            endMethod()
        }
        else DummyTask(WeakReference(this), this, processID, runID, serverState, phoneNumber, mode).execute()
    }
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun requestDefaultApp() {
        val packageName = this.packageName
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
        startActivityForResult(intent, 0)
    }


    private fun initialiseOptions() {
        val options = getLastProcess(this)
        processID = options[0] as Long
        serverState = options[2] as Boolean
        //phoneNumber = intent.getStringExtra("phone_number")
    }
    fun nextActivity(view: View) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
        startActivity(Intent(this, MetadataActivity ::class.java))
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
    }
    override fun onBackPressed() {

    }

    override fun processFinish(output: Any) {
        when (output) {
            -1 -> failedProcedure()
            Mode.DUMMY.ordinal -> endMethod()
            else -> runListening(output)
        }
    }

    private fun endMethod() {
        defaultDescriptionTextView.text = "${getString(R.string.default_description)}\n${getString(R.string.method_ok)}"
        defaultLaunchButton.isEnabled = false
        defaultNextButton.visibility = View.VISIBLE
    }

    private fun failedProcedure() {
        defaultDescriptionTextView.text = "${getString(R.string.default_description)}\n${getString(R.string.method_failed)}"
        defaultNextButton.visibility = View.VISIBLE
        defaultLaunchButton.isEnabled = false
        updateTokenMethod(this, runID, false)
    }

    private fun runListening(output: Any) {
        defaultDescriptionTextView.text = "${getString(R.string.default_description)}\n${getString(R.string.activation_code)}" +
                "$output\n${getString(R.string.method_tested)}"
        mode = Mode.DUMMY
        defaultNextButton.visibility = View.VISIBLE
        updateTokenMethod(this, runID, true)

    }
}
