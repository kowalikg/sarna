package pl.edu.agh.sarna.smsToken

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_token_sms.*
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.metadata.MetadataActivity
import pl.edu.agh.sarna.permissions.checkReadSmsPermission
import pl.edu.agh.sarna.permissions.checkSendSmsPermission
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.smsToken.task.method.NotSafeTask
import pl.edu.agh.sarna.smsToken.task.method.SafeTokenTask
import pl.edu.agh.sarna.smsToken.task.method.DummyTask
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.isDefaultSmsApp
import pl.edu.agh.sarna.utils.kotlin.isNetworkAvailable
import java.lang.ref.WeakReference


class TokenSms : AppCompatActivity(), AsyncResponse {
    private var rootState: Boolean = false
    private var eduState: Boolean = false
    private var serverState: Boolean = false
    private var reportState: Boolean = false
    private var processID: Long = 0

    private var sendSmsPermissionGranted = false
    private var readSmsPermissionGranted = false

    private var phoneNumber : String = "+48731464100"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_token_sms)
        initialiseOptions()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun startTokenMethod(view: View) {
        //if(!verifyPhoneNumber()) return
        safeJob()
    }

    override fun onFirstFinished(output: Any) {
        if(output as Int == 0){
            notSafeJob()
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onSecondFinished(output: Any) {
        dummyJob()
    }

    private fun verifyPhoneNumber(): Boolean {
        phoneNumber = tokenEditText.text.toString()
        if (phoneNumber.isEmpty()) {
            tokenEditText.error = "Please input phone number!"
            return false
        }
        return true
    }

    private fun notSafeJob() {
        if (!checkPermissions())
            requestSelectedPermissions()
        else {
            classicTokenJob()
        }
    }

    private fun safeJob() {
        SafeTokenTask(WeakReference(this), this, processID).execute()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun dummyJob() {
        if (isDefaultSmsApp(this)) {
            dummyTask()
        } else {
            requestDefaultApp()

        }
    }
    private fun dummyTask(){
        DummyTask(WeakReference(this), this, processID, serverState, phoneNumber).execute()
    }
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun requestDefaultApp() {
        val packageName = this.packageName
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        dummyTask()
    }

    private fun nextActivity() {
        startActivity(Intent(this, MetadataActivity::class.java).apply {
            putExtra("root_state", rootState)
            putExtra("edu_state", eduState)
            putExtra("report_state", reportState)
            putExtra("server_state", serverState)
            putExtra("process_id", processID)
        })
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
    }

    private fun checkPermissions(): Boolean {
        readSmsPermissionGranted = checkReadSmsPermission(this)
        sendSmsPermissionGranted = checkSendSmsPermission(this)
        return readSmsPermissionGranted and sendSmsPermissionGranted
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestSelectedPermissions() {
        if (!serverState and !isNetworkAvailable(this) and !readSmsPermissionGranted and !sendSmsPermissionGranted)
            requestPermissions(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS), 10)
        else if(!serverState and !isNetworkAvailable(this) and !sendSmsPermissionGranted)
            requestPermissions(arrayOf(Manifest.permission.SEND_SMS), 20)
        else if(!readSmsPermissionGranted)
            requestPermissions(arrayOf(Manifest.permission.READ_SMS), 30)

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        classicTokenJob()
    }

    private fun classicTokenJob() {
        NotSafeTask(WeakReference(this), this, processID, serverState, phoneNumber).execute()
    }

    override fun processFinish(output: Any) {
        if(output == 0) nextActivity()
    }

    private fun initialiseOptions() {
        rootState = intent.getBooleanExtra("root_state", false)
        eduState = intent.getBooleanExtra("edu_state", false)
        serverState = intent.getBooleanExtra("server_state", false)
        reportState = intent.getBooleanExtra("report_state", false)
        processID = intent.getLongExtra("process_id", 0)
    }

    override fun onBackPressed() {

    }


}
