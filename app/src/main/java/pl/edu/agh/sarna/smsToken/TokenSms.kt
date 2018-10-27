package pl.edu.agh.sarna.smsToken

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.telephony.SmsManager
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_token_sms.*
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.metadata.MetadataActivity
import pl.edu.agh.sarna.permissions.checkReadSmsPermission
import pl.edu.agh.sarna.permissions.checkSendSmsPermission
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.smsToken.model.SmsMessage
import pl.edu.agh.sarna.smsToken.task.ClassicTokenTask
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.isDefaultSmsApp
import pl.edu.agh.sarna.utils.kotlin.isKitKat4_4
import pl.edu.agh.sarna.utils.kotlin.isOreo8_0


class TokenSms : AppCompatActivity(), AsyncResponse {
    private var rootState: Boolean = false
    private var eduState: Boolean = false
    private var serverState: Boolean = false
    private var reportState: Boolean = false
    private var processID: Long = 0

    private var mode: Mode = Mode.NOT_SAFE

    private var permissionsGranted = false
    private var sendSmsPermissionGranted = false
    private var readSmsPermissionGranted = false

    private var defaultSmsApp = false

    private var phoneNumber : String = "+48731464100"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_token_sms)
        initialiseOptions()
        initialiseLayout()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun startTokenMethod(view: View) {
        //if(!verifyPhoneNumber()) return

        when (mode){
            Mode.SAFE -> safeJob()
            Mode.NOT_SAFE -> notSafeJob()
            Mode.DUMMY -> dummyJob()
        }

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
            permissionsGranted = true
            classicTokenJob()
        }
    }

    private fun safeJob() {

    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun dummyJob() {
        if (isDefaultSmsApp(this)) {
            notSafeJob()
        } else {
            requestDefaultApp()

        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun requestDefaultApp() {
        val packageName = this.packageName
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        defaultSmsApp = resultCode == -1
        notSafeJob()
    }

    private fun checkPermissions(): Boolean {
        readSmsPermissionGranted = checkReadSmsPermission(this)
        sendSmsPermissionGranted = checkSendSmsPermission(this)
        return readSmsPermissionGranted and sendSmsPermissionGranted
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestSelectedPermissions() {
        if (!readSmsPermissionGranted and !sendSmsPermissionGranted)
            requestPermissions(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS), 10)
        else if(!readSmsPermissionGranted)
            requestPermissions(arrayOf(Manifest.permission.READ_SMS), 20)
        else if(!sendSmsPermissionGranted)
            requestPermissions(arrayOf(Manifest.permission.SEND_SMS), 30)

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        readSmsPermissionGranted = checkReadSmsPermission(this)
        sendSmsPermissionGranted = checkSendSmsPermission(this)
        permissionsGranted = readSmsPermissionGranted and sendSmsPermissionGranted
        classicTokenJob()
    }

    private fun classicTokenJob() {
        ClassicTokenTask(this, this, defaultSmsApp, readSmsPermissionGranted).execute()
    }

    override fun processFinish(output: Any) {
        if (output == 0) startActivity(Intent(this, MetadataActivity::class.java).apply {
            putExtra("root_state", rootState)
            putExtra("edu_state", eduState)
            putExtra("report_state", reportState)
            putExtra("server_state", serverState)
            putExtra("process_id", processID)
        })
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
    }
    private fun initialiseLayout() {
        initialiseSafeRadio()
        initialiseNotSafeRadio()
        initialiseDummyRadio()

    }

    private fun initialiseDummyRadio() {
        dummyRadio.isEnabled = isKitKat4_4()
        dummyRadio.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) mode = Mode.DUMMY
        }
    }

    private fun initialiseNotSafeRadio() {
        notSafeRadio.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) mode = Mode.NOT_SAFE
        }
    }

    private fun initialiseSafeRadio() {
        safeRadio.isEnabled = isOreo8_0()
        safeRadio.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) mode = Mode.SAFE
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
