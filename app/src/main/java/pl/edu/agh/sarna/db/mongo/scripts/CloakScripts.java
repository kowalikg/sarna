package pl.edu.agh.sarna.db.mongo.scripts;

import org.bson.Document;

import pl.edu.agh.sarna.db.model.cloak.CloakInfo;
import pl.edu.agh.sarna.db.model.cloak.CloakText;
import pl.edu.agh.sarna.db.mongo.MongoDb;

public class CloakScripts {

    public static void saveCloakInfoToMongo(long processID, long startTime, long endTime,
                                            boolean status) {
        final String TABLE_NAME = CloakInfo.CloakInfoEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(CloakInfo.CloakInfoEntry.COLUMN_NAME_PROCESS_ID, processID)
                .append(CloakInfo.CloakInfoEntry.COLUMN_NAME_START_TIME, startTime)
                .append(CloakInfo.CloakInfoEntry.COLUMN_NAME_END_TIME, endTime)
                .append(CloakInfo.CloakInfoEntry.COLUMN_NAME_STATUS, status);
        mongoDb.saveData(TABLE_NAME, document);
    }

    public static void saveCloakTextToMongo(/* long runID, */ String text, String package_) {
        final String TABLE_NAME = CloakText.CloakTextEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
//                .append(CloakText.CloakTextEntry.COLUMN_NAME_RUN_ID, runID)
                .append(CloakText.CloakTextEntry.COLUMN_NAME_TEXT, text)
                .append(CloakText.CloakTextEntry.COLUMN_NAME_PACKAGE, package_);
        mongoDb.saveData(TABLE_NAME, document);
    }
    
}
