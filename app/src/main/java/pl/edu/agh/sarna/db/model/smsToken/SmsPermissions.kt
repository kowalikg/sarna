package pl.edu.agh.sarna.db.model.smsToken

import android.provider.BaseColumns

object SmsPermissions {
    object SmsPermissionsEntry : BaseColumns {
        const val TABLE_NAME = "SmsPermissions"
        const val COLUMN_NAME_RUN_ID = "RunID"
        const val COLUMN_NAME_READ = "Read"
        const val COLUMN_NAME_RECEIVE = "Receive"
        const val COLUMN_NAME_SEND = "Send"
    }
}