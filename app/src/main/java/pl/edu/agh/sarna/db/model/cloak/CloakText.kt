package pl.edu.agh.sarna.db.model.cloak

import android.provider.BaseColumns

object CloakText {
    object CloakTextEntry : BaseColumns {
        const val TABLE_NAME = "CloakText"
        const val COLUMN_NAME_RUN_ID = "RunID"
        const val COLUMN_NAME_TEXT = "Text"
        const val COLUMN_NAME_PACKAGE = "Package"
    }
}