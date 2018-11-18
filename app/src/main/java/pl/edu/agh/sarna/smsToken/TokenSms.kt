package pl.edu.agh.sarna.smsToken

import android.Manifest
import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_token_sms.*
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.scripts.codesAmount
import pl.edu.agh.sarna.db.scripts.insertSmsPermissions
import pl.edu.agh.sarna.db.scripts.insertTokenQuery
import pl.edu.agh.sarna.db.scripts.updateTokenMethod
import pl.edu.agh.sarna.permissions.checkReadSmsPermission
import pl.edu.agh.sarna.permissions.checkSendSmsPermission
import pl.edu.agh.sarna.report.ReportActivity
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.smsToken.task.method.NotSafeTask
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.isNetworkAvailable
import java.lang.ref.WeakReference


class TokenSms : AppCompatActivity(), AsyncResponse {
    private var rootState: Boolean = false
    private var eduState: Boolean = false
    private var serverState: Boolean = false
    private var reportState: Boolean = false
    private var processID: Long = 0
    private var mode = Mode.TEST
    private var sendSmsPermissionGranted = false
    private var readSmsPermissionGranted = false

    private var runID: Long = 0

    private var phoneNumber : String = "+48731464100"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_token_sms)
        initialiseOptions()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun startTokenMethod(view: View) {
        //if(!verifyPhoneNumber()) return
        notSafeJob()
    }

    private fun verifyPhoneNumber(): Boolean {
        phoneNumber = tokenEditText.text.toString()
        if (phoneNumber.isEmpty()) {
            tokenEditText.error = getString(R.string.number_error)
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

    fun nextActivity(view: View) {
        startActivity(Intent(this, DefaultSms::class.java).apply {
            putExtra("root_state", rootState)
            putExtra("edu_state", eduState)
            putExtra("report_state", reportState)
            putExtra("server_state", serverState)
            putExtra("process_id", processID)
            putExtra("number", phoneNumber)
        })
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
    }

    private fun checkPermissions(): Boolean {
        readSmsPermissionGranted = checkReadSmsPermission(this)
        if(serverState and isNetworkAvailable(this)) return readSmsPermissionGranted
        sendSmsPermissionGranted = checkSendSmsPermission(this)
        return readSmsPermissionGranted and sendSmsPermissionGranted
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestSelectedPermissions() {
        val sendByNetwork = serverState and isNetworkAvailable(this)
        if (!sendByNetwork and !readSmsPermissionGranted and !sendSmsPermissionGranted)
            requestPermissions(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS), 10)
        else if(!sendByNetwork and !sendSmsPermissionGranted)
            requestPermissions(arrayOf(Manifest.permission.SEND_SMS), 20)
        else if(!readSmsPermissionGranted)
            requestPermissions(arrayOf(Manifest.permission.READ_SMS), 30)

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        classicTokenJob()
    }


    private fun classicTokenJob() {
        runID = insertTokenQuery(this, processID, mode.ordinal)!!
        insertSmsPermissions(this, runID)
        NotSafeTask(WeakReference(this), this, processID, runID, serverState, phoneNumber, mode).execute()
    }


    override fun processFinish(output: Any) {
        when (output) {
            Mode.TEST.ordinal -> runListening()
            -1 -> failedProcedure()
            Mode.NOT_SAFE.ordinal -> endMethod()
        }
    }

    private fun endMethod() {
        defaultButton.isEnabled = false
        smsDescriptionTextView.text = "OK"
        updateTokenMethod(this, runID, codesAmount(this, runID) > 0)
    }

    private fun failedProcedure() {
        smsDescriptionTextView.text = "FAILED"
        nextButton.visibility = View.VISIBLE
        defaultButton.isEnabled = false
        updateTokenMethod(this, runID, false)
    }

    private fun runListening() {
        smsDescriptionTextView.text = "TESTED"
        mode = Mode.NOT_SAFE
        nextButton.visibility = View.VISIBLE
        updateTokenMethod(this, runID, true)

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
