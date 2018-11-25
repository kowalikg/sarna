package pl.edu.agh.sarna.smsToken

import android.content.Context
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.smsToken.model.SmsMessage
import java.lang.ref.WeakReference

class Extractor(private val contextReference: WeakReference<Context>) {
    fun extract(list: ArrayList<SmsMessage>, test : Boolean = false) : List<SmsMessage> {
        val codeList = ArrayList<SmsMessage?>()
        for (sms in list){
            codeList.add(extractCode(sms, test))
        }
        return codeList.filterNotNull()
    }
    fun extract(sms : SmsMessage, test : Boolean = false): SmsMessage? {
        return extractCode(sms, test)
    }
    private fun extractCode(sms: SmsMessage, test : Boolean = false): SmsMessage? {
        val patterns = arrayListOf(
                """${contextReference.get()!!.getString(R.string.password_regexp)}(\d+)""", """test:[ ]*(\d+)""")
        if (test) patterns.add("""${contextReference.get()!!.getString(R.string.password_regexp)}(\d+)""")

        for (pattern in patterns){
            val regex = pattern.toRegex()
            val matchResult = regex.find(sms.content) ?: continue
            val group = matchResult.destructured.match.groups[1] ?: continue
            return SmsMessage(sms.id, sms.number, group.value)
        }
        return null
    }
}