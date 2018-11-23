package pl.edu.agh.sarna.db.mongo;

import com.mongodb.MongoException;

public class MongoDbException extends Exception {

    public MongoDbException(MongoException e) {
        super("Could not connect to MongoDB. No internet connection or invalid credentials.", e);
    }

}