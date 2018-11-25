package pl.edu.agh.sarna.db.mongo.scripts;

import org.bson.Document;

import pl.edu.agh.sarna.db.model.wifi.WifiPasswords;
import pl.edu.agh.sarna.db.model.wifi.WifiUtils;
import pl.edu.agh.sarna.db.mongo.MongoDb;

public class WifiScripts {

    public static void saveWifiUtilsToMongo(long runID, boolean storagePermission, boolean
            locationPermission, boolean wifiConnectedStatus, boolean passwordFoundStatus, String
            wifiSSID, String wifiPassword) {
        final String TABLE_NAME = WifiUtils.WifiUtilsEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(WifiUtils.WifiUtilsEntry.COLUMN_NAME_RUN_ID, runID)
                .append(WifiUtils.WifiUtilsEntry.COLUMN_NAME_STORAGE_PERMISSION_STATUS, storagePermission)
                .append(WifiUtils.WifiUtilsEntry.COLUMN_NAME_LOCATION_PERMISSION_STATUS, locationPermission)
                .append(WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_CONNECTED_STATUS, wifiConnectedStatus)
                .append(WifiUtils.WifiUtilsEntry.COLUMN_NAME_PASSWORD_FOUND_STATUS, passwordFoundStatus)
                .append(WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_SSID, wifiSSID)
                .append(WifiUtils.WifiUtilsEntry.COLUMN_NAME_WIFI_PASSWORD, wifiPassword);
        mongoDb.saveData(TABLE_NAME, document);
    }

    public static void saveWifiPasswordsToMongo(long processID, long startTime, long endTime,
                                                boolean status) {
        final String TABLE_NAME = WifiPasswords.WifiPasswordsEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_PROCESS_ID, processID)
                .append(WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_START_TIME, startTime)
                .append(WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_END_TIME, endTime)
                .append(WifiPasswords.WifiPasswordsEntry.COLUMN_NAME_STATUS, status);
        mongoDb.saveData(TABLE_NAME, document);
    }

}
