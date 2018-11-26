package pl.edu.agh.sarna.db.mongo;

import android.util.Log;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.bson.Document;

public class MongoDb {

    private MongoDbConnection connection;

    private MongoDatabase database;

    public static final String LOG_TAG = "mongo_db";

    public static final String ERROR_MSG = "Could not connect to MongoDB. No internet connection " +
            "or invalid credentials.";

    public MongoDb() {
        connection = MongoDbConnection.getDefaultConnection();
        MongoClient client = new MongoClient(connection.getServerAddress(),
                connection.getCredentialList(), connection.getOptions());
        database = client.getDatabase(connection.getDatabaseName());
    }

    public void saveData(String collectionName, Document document) {
        new Thread(() -> {
            try {
                database.getCollection(collectionName).insertOne(document);
            } catch (MongoException e) {
                Log.e(LOG_TAG, ERROR_MSG);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }).start();
    }

}