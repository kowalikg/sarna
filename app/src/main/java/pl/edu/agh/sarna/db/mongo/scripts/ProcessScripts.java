package pl.edu.agh.sarna.db.mongo.scripts;

import android.util.Log;

import org.bson.Document;

import pl.edu.agh.sarna.db.model.Processes;
import pl.edu.agh.sarna.db.model.wifi.WifiPasswords;
import pl.edu.agh.sarna.db.mongo.MongoDb;
import pl.edu.agh.sarna.db.mongo.MongoDbException;

public class ProcessScripts {

    public static void saveProcessesToMongo(long startTime, long endTime, int externalServer,
                                            boolean rootAllowed, boolean educational, boolean
                                                    report, float systemVersion) {
        final String TABLE_NAME = Processes.ProcessEntry.TABLE_NAME;
        MongoDb mongoDb = new MongoDb();
        Document document = new Document()
                .append(Processes.ProcessEntry.COLUMN_NAME_START_TIME, startTime)
                .append(Processes.ProcessEntry.COLUMN_NAME_END_TIME, endTime)
                .append(Processes.ProcessEntry.COLUMN_NAME_EXTERNAL_SERVER, externalServer)
                .append(Processes.ProcessEntry.COLUMN_NAME_ROOT_ALLOWED, rootAllowed)
                .append(Processes.ProcessEntry.COLUMN_NAME_EDUCATIONAL, educational)
                .append(Processes.ProcessEntry.COLUMN_NAME_REPORT, report)
                .append(Processes.ProcessEntry.COLUMN_NAME_SYSTEM_VERSION, systemVersion);
        try {
            mongoDb.saveData(TABLE_NAME, document);
        } catch (MongoDbException e) {
            Log.e(MongoDb.LOG_TAG, e.getMessage());
        }
    }

}
