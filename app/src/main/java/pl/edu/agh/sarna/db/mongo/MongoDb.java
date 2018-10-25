package pl.edu.agh.sarna.db.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import org.bson.Document;

public class MongoDb {

    private MongoDbCredentials credentials;

    private MongoClient client;

    private MongoDatabase database;

    public MongoDb() {
        credentials = MongoDbCredentials.getDefaultCredentials();
        client = credentials.getClient();
        database = client.getDatabase(credentials.getDatabaseName());
    }

    public void saveData(String collectionName, Document document) throws MongoDbException {
        try {
            database.getCollection(collectionName).insertOne(document);
        } catch (MongoException e) {
            throw new MongoDbException(e);
        }
    }

    public MongoCollection<Document> loadData(String collectionName) throws MongoDbException {
        try {
            return database.getCollection(collectionName);
        } catch (MongoException e) {
            throw new MongoDbException(e);
        }
    }

    public MongoIterable<String> getCollections() throws MongoDbException {
        try {
            return database.listCollectionNames();
        } catch (MongoException e) {
            throw new MongoDbException(e);
        }
    }

    public void closeConnection() {
        client.close();
    }

}
