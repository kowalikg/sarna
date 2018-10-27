package pl.edu.agh.sarna.smsToken.dummy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.support.annotation.RequiresApi
import pl.edu.agh.sarna.smsToken.Extractor
import pl.edu.agh.sarna.smsToken.model.SmsMessage


class IncomingSms : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onReceive(p0: Context?, p1: Intent?) {
        for (smsMessage in Telephony.Sms.Intents.getMessagesFromIntent(p1)) {
            val smsSender = smsMessage.displayOriginatingAddress
            val smsBody = smsMessage.messageBody
            val codes = Extractor(arrayListOf(SmsMessage(0, smsSender, smsBody))).extract()

        }

    }
}