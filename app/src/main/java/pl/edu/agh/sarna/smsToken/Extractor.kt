package pl.edu.agh.sarna.smsToken

import android.content.Context
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.smsToken.model.SmsMessage
import java.lang.ref.WeakReference

class Extractor(private val contextReference: WeakReference<Context>) {
    fun extract(list: ArrayList<SmsMessage>) : List<SmsMessage> {
        val codeList = ArrayList<SmsMessage?>()
        for (sms in list){
            codeList.add(extractCode(sms))
        }
        return codeList.filterNotNull()
    }
    fun extract(sms : SmsMessage): SmsMessage? {
        return extractCode(sms)
    }
    private fun extractCode(sms: SmsMessage): SmsMessage? {
        val patterns = arrayOf(
                """${contextReference.get()!!.getString(R.string.password_regexp)}(\d+)"""
        )
        for (pattern in patterns){
            val regex = pattern.toRegex()
            val matchResult = regex.find(sms.content) ?: continue
            val group = matchResult.destructured.match.groups[1] ?: continue
            return SmsMessage(sms.id, sms.number, group.value)
        }
        return null
    }
}