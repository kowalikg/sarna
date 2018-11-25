package pl.edu.agh.sarna.db.mongo.scripts;

import org.bson.Document;

import pl.edu.agh.sarna.db.model.calls.CallsDetails;
import pl.edu.agh.sarna.db.model.calls.CallsLogs;
import pl.edu.agh.sarna.db.model.calls.CallsLogsInfo;
import pl.edu.agh.sarna.db.model.contacts.Contacts;
import pl.edu.agh.sarna.db.model.contacts.ContactsInfo;
import pl.edu.agh.sarna.db.mongo.MongoDb;

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
        mongoDb.saveData(TABLE_NAME, document);
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
        mongoDb.saveData(TABLE_NAME, document);
    }

    public static void saveCallsLogsInfoToMongo(long runID, boolean logPermission, boolean found) {
        final String TABLE_NAME = CallsLogsInfo.CallsLogsInfoEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_RUN_ID, runID)
                .append(CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_LOG_PERMISSION, logPermission)
                .append(CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_FOUND, found);
        mongoDb.saveData(TABLE_NAME, document);
    }

    public static void saveContactsInfoToMongo(long runID, boolean contactsPermission, boolean
            found) {
        final String TABLE_NAME = ContactsInfo.ContactsInfoEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(ContactsInfo.ContactsInfoEntry.COLUMN_NAME_RUN_ID, runID)
                .append(ContactsInfo.ContactsInfoEntry.COLUMN_NAME_CONTACTS_PERMISSION, contactsPermission)
                .append(ContactsInfo.ContactsInfoEntry.COLUMN_NAME_FOUND, found);
        mongoDb.saveData(TABLE_NAME, document);
    }

    public static void saveContactsToMongo(long runID, String name, String number) {
        final String TABLE_NAME = Contacts.ContactsEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(Contacts.ContactsEntry.COLUMN_NAME_RUN_ID, runID)
                .append(Contacts.ContactsEntry.COLUMN_NAME_NAME, name)
                .append(Contacts.ContactsEntry.COLUMN_NAME_NUMBER, number);
        mongoDb.saveData(TABLE_NAME, document);
    }

}
