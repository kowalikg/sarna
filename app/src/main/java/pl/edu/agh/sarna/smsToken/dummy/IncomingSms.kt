package pl.edu.agh.sarna.smsToken.dummy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.support.annotation.RequiresApi
import pl.edu.agh.sarna.db.scripts.getLastRunID
import pl.edu.agh.sarna.db.scripts.insertCodes
import pl.edu.agh.sarna.smsToken.Extractor
import pl.edu.agh.sarna.smsToken.model.SmsMessage

class IncomingSms : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onReceive(p0: Context?, p1: Intent?) {
        val runID = getLastRunID(p0)
        val codes = ArrayList<SmsMessage>()
        for (smsMessage in Telephony.Sms.Intents.getMessagesFromIntent(p1)) {
            codes.add(Extractor().extract(SmsMessage(
                            0, smsMessage.displayOriginatingAddress, smsMessage.messageBody))!!)
        }
        codes.forEach {  insertCodes(p0!!, runID, it)}

    }
}