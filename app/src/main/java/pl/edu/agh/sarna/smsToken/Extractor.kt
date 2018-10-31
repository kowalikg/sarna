package pl.edu.agh.sarna.smsToken

import pl.edu.agh.sarna.smsToken.model.SmsMessage

class Extractor {
    private val patterns = arrayOf("""haslo: (\d+)""")
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
        for (pattern in patterns){
            val regex = pattern.toRegex()
            val matchResult = regex.find(sms.content) ?: continue
            val group = matchResult.destructured.match.groups[1] ?: continue
            return SmsMessage(sms.id, sms.number, group.value)
        }
        return null
    }
}