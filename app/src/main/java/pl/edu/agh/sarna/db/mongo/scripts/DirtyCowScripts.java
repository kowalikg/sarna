package pl.edu.agh.sarna.db.mongo.scripts;

import org.bson.Document;

import pl.edu.agh.sarna.db.model.dirtycow.DirtyCowDetails;
import pl.edu.agh.sarna.db.model.dirtycow.DirtyCowInfo;
import pl.edu.agh.sarna.db.mongo.MongoDb;

public class DirtyCowScripts {

    public static void saveDirtyCowDetailsToMongo(long processID, long startTime, long endTime,
                                                  boolean status) {
        final String TABLE_NAME = DirtyCowDetails.DirtyCowDetailsEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_PROCESS_ID, processID)
                .append(DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_START_TIME, startTime)
                .append(DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_END_TIME, endTime)
                .append(DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_STATUS, status);
        mongoDb.saveData(TABLE_NAME, document);
    }

    public static void saveDirtyCowInfoToMongo(long runID, long eta, boolean seLinux, String kernel,
                                               String build, String vendor) {
        final String TABLE_NAME = DirtyCowInfo.DirtyCowInfoEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_RUN_ID, runID)
                .append(DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_ETA, eta)
                .append(DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_SELINUX, seLinux)
                .append(DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_KERNEL, kernel)
                .append(DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_BUILD, build)
                .append(DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_VENDOR, vendor);
        mongoDb.saveData(TABLE_NAME, document);
    }

}
