package pl.edu.agh.sarna.db.mongo.scripts;

import android.util.Log;

import org.bson.Document;

import pl.edu.agh.sarna.db.mongo.MongoDb;
import pl.edu.agh.sarna.db.model.calls.*;
import pl.edu.agh.sarna.db.model.contacts.*;
import pl.edu.agh.sarna.db.mongo.MongoDbException;

public class CallScripts {

    public static void saveCallsDetailsToMongo(long processID, long startTime, long endTime,
                                               boolean status) {
        final String TABLE_NAME = CallsDetails.CallsDetailsEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(CallsDetails.CallsDetailsEntry.COLUMN_NAME_PROCESS_ID, processID)
                .append(CallsDetails.CallsDetailsEntry.COLUMN_NAME_START_TIME, startTime)
                .append(CallsDetails.CallsDetailsEntry.COLUMN_NAME_END_TIME, endTime)
                .append(CallsDetails.CallsDetailsEntry.COLUMN_NAME_STATUS, status);
        try {
            mongoDb.saveData(TABLE_NAME, document);
        } catch (MongoDbException e) {
            Log.e(MongoDb.LOG_TAG, e.getMessage());
        }
    }

    public static void saveCallsLogsToMongo(long runID, String name, String number, int type,
                                            String date, String duration) {
        final String TABLE_NAME = CallsLogs.CallsLogsEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(CallsLogs.CallsLogsEntry.COLUMN_NAME_RUN_ID, runID)
                .append(CallsLogs.CallsLogsEntry.COLUMN_NAME_NAME, name)
                .append(CallsLogs.CallsLogsEntry.COLUMN_NAME_NUMBER, number)
                .append(CallsLogs.CallsLogsEntry.COLUMN_NAME_TYPE, type)
                .append(CallsLogs.CallsLogsEntry.COLUMN_NAME_DATE, date)
                .append(CallsLogs.CallsLogsEntry.COLUMN_NAME_DURATION, duration);
        try {
            mongoDb.saveData(TABLE_NAME, document);
        } catch (MongoDbException e) {
            Log.e(MongoDb.LOG_TAG, e.getMessage());
        }
    }

    public static void saveCallsLogsInfoToMongo(long runID, boolean logPermission, boolean found) {
        final String TABLE_NAME = CallsLogsInfo.CallsLogsInfoEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_RUN_ID, runID)
                .append(CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_LOG_PERMISSION, logPermission)
                .append(CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_FOUND, found);
        try {
            mongoDb.saveData(TABLE_NAME, document);
        } catch (MongoDbException e) {
            Log.e(MongoDb.LOG_TAG, e.getMessage());
        }
    }

    public static void saveContactsInfoToMongo(long runID, boolean contactsPermission, boolean
            found) {
        final String TABLE_NAME = ContactsInfo.ContactsInfoEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(ContactsInfo.ContactsInfoEntry.COLUMN_NAME_RUN_ID, runID)
                .append(ContactsInfo.ContactsInfoEntry.COLUMN_NAME_CONTACTS_PERMISSION, contactsPermission)
                .append(ContactsInfo.ContactsInfoEntry.COLUMN_NAME_FOUND, found);
        try {
            mongoDb.saveData(TABLE_NAME, document);
        } catch (MongoDbException e) {
            Log.e(MongoDb.LOG_TAG, e.getMessage());
        }
    }

    public static void saveContactsToMongo(long runID, String name, String number) {
        final String TABLE_NAME = Contacts.ContactsEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(Contacts.ContactsEntry.COLUMN_NAME_RUN_ID, runID)
                .append(Contacts.ContactsEntry.COLUMN_NAME_NAME, name)
                .append(Contacts.ContactsEntry.COLUMN_NAME_NUMBER, number);
        try {
            mongoDb.saveData(TABLE_NAME, document);
        } catch (MongoDbException e) {
            Log.e(MongoDb.LOG_TAG, e.getMessage());
        }
    }

}
