package pl.edu.agh.sarna.db.model

import android.provider.BaseColumns

object WifiUtils {
    object WifiUtilsEntry : BaseColumns {
        const val TABLE_NAME = "WifiUtils"
        const val COLUMN_NAME_RUN_ID = "RunID"
        const val COLUMN_NAME_LOCATION_PERMISSION_STATUS = "Location_status"
        const val COLUMN_NAME_STORAGE_PERMISSION_STATUS = "Storage_status"
        const val COLUMN_NAME_WIFI_CONNECTED_STATUS = "Connected_status"
        const val COLUMN_NAME_PASSWORD_FOUND_STATUS = "Password_status"
        const val COLUMN_NAME_WIFI_SSID = "SSID"
        const val COLUMN_NAME_WIFI_PASSWORD = "Password"
    }
}