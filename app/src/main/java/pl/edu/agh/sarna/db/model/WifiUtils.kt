package pl.edu.agh.sarna.db.model

import android.provider.BaseColumns

object WifiUtils {
    object WifiUtilsEntry : BaseColumns {
        const val TABLE_NAME = "WifiUtils"
        const val COLUMN_NAME_RUN_ID = "RunID"
        const val COLUMN_NAME_LOCATION_PERMISSION_STATUS = "LocationStatus"
        const val COLUMN_NAME_STORAGE_PERMISSION_STATUS = "StorageStatus"
        const val COLUMN_NAME_WIFI_CONNECTED_STATUS = "ConnectedStatus"
        const val COLUMN_NAME_PASSWORD_FOUND_STATUS = "PasswordStatus"
    }
}