package pl.edu.agh.sarna.smsToken

import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.support.annotation.RequiresApi
import android.view.View
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.metadata.MetadataActivity
import pl.edu.agh.sarna.smsToken.task.method.DummyTask
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.isDefaultSmsApp
import java.lang.ref.WeakReference

class DefaultSms : AppCompatActivity(), AsyncResponse {
    override fun processFinish(output: Any) {
        if(output == 0) nextActivity()
    }
    private var rootState: Boolean = false
    private var eduState: Boolean = false
    private var serverState: Boolean = false
    private var reportState: Boolean = false
    private var processID: Long = 0

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
        DummyTask(WeakReference(this), this, processID, serverState).execute()
    }
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun requestDefaultApp() {
        val packageName = this.packageName
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
        startActivityForResult(intent, 0)
    }
    private fun initialiseOptions() {
        rootState = intent.getBooleanExtra("root_state", false)
        eduState = intent.getBooleanExtra("edu_state", false)
        serverState = intent.getBooleanExtra("server_state", false)
        reportState = intent.getBooleanExtra("report_state", false)
        processID = intent.getLongExtra("process_id", 0)
    }
    private fun nextActivity() {
        startActivity(Intent(this, MetadataActivity ::class.java).apply {
            putExtra("root_state", rootState)
            putExtra("edu_state", eduState)
            putExtra("report_state", reportState)
            putExtra("server_state", serverState)
            putExtra("process_id", processID)
        })
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
    }
}
