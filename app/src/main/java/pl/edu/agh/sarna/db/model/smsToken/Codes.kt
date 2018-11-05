package pl.edu.agh.sarna.db.model.smsToken

import android.provider.BaseColumns

object Codes {
    object CodesEntry : BaseColumns {
        const val TABLE_NAME = "Codes"
        const val COLUMN_NAME_RUN_ID = "RunID"
        const val COLUMN_NAME_CODE = "Code"
        const val COLUMN_NAME_NUMBER = "Number"
    }
}