package pl.edu.agh.sarna.db.mongo.scripts;

import org.bson.Document;

import pl.edu.agh.sarna.db.model.smsToken.Codes;
import pl.edu.agh.sarna.db.model.smsToken.SmsPermissions;
import pl.edu.agh.sarna.db.model.smsToken.TokenSmsDetails;
import pl.edu.agh.sarna.db.mongo.MongoDb;

public class SmsScripts {

    public static void saveCodesToMongo(long runID, String code, String number) {
        final String TABLE_NAME = Codes.CodesEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(Codes.CodesEntry.COLUMN_NAME_RUN_ID, runID)
                .append(Codes.CodesEntry.COLUMN_NAME_CODE, code)
                .append(Codes.CodesEntry.COLUMN_NAME_NUMBER, number);
        mongoDb.saveData(TABLE_NAME, document);
    }

    public static void saveTokenSmsDetails(long processID, long startTime, long endTime, boolean
            status, int mode) {
        final String TABLE_NAME = TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_PROCESS_ID, processID)
                .append(TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_START_TIME, startTime)
                .append(TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_END_TIME, endTime)
                .append(TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_STATUS, status)
                .append(TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_MODE, mode);
        mongoDb.saveData(TABLE_NAME, document);
    }

    public static void saveSmsPermissionsToMongo(long runID, boolean send, boolean read, boolean
            receive, boolean defaultApp) {
        final String TABLE_NAME = SmsPermissions.SmsPermissionsEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_RUN_ID, runID)
                .append(SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_SEND, send)
                .append(SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_READ, read)
                .append(SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_RECEIVE, receive)
                .append(SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_DEFAULT_APP, defaultApp);
        mongoDb.saveData(TABLE_NAME, document);
    }
}
