package pl.edu.agh.sarna.smsToken

import pl.edu.agh.sarna.smsToken.model.SmsMessage

class Extractor() {
    private val patterns = arrayOf("""haslo: (\d+)""")
    fun extract(list: ArrayList<SmsMessage>) : List<String> {
        val codeList = ArrayList<String?>()
        for (sms in list){
            codeList.add(extractCode(sms.content))
        }
        return codeList.filterNotNull()
    }
    fun extract(sms : SmsMessage): String? {
        return extractCode(sms.content)
    }
    private fun extractCode(content: String): String? {
        for (pattern in patterns){
            val regex = pattern.toRegex()
            val matchResult = regex.find(content) ?: continue
            val group = matchResult.destructured.match.groups[1] ?: continue
            return group.value
        }
        return null
    }
}