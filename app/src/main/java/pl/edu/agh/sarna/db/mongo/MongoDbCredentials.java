package pl.edu.agh.sarna.db.mongo;

import android.util.Log;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

public class MongoDbCredentials {

    private String user;

    private String password;

    private String host;

    private int port;

    private String databaseName;

    private static final String DEFAULT_USER = "student";

    private static final String DEFAULT_PASSWORD = "Student@1234";

    private static final String DEFAULT_HOST = "ds137643.mlab.com";

    private static final int DEFAULT_PORT = 37643;

    private static final String DEFAULT_DATABASE_NAME = "sarna-db";

    private static String encodeString(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.wtf("URLEncoder", e);
            return str;
        }
    }

    private MongoClientURI getUri() {
        String encodedUser = encodeString(user);
        String encodedPassword = encodeString(password);
        String encodedHost = encodeString(host);
        String encodedDatabaseName = encodeString(databaseName);

        String connectionString = String.format(Locale.getDefault(),"mongodb://%s:%s@%s:%d/%s",
                encodedUser, encodedPassword, encodedHost, port, encodedDatabaseName);
        return new MongoClientURI(connectionString);
    }

    public MongoDbCredentials(String user, String password, String host, int port, String databaseName) {
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
    }

    public static MongoDbCredentials getDefaultCredentials() {
        return new MongoDbCredentials(
                DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_HOST, DEFAULT_PORT, DEFAULT_DATABASE_NAME);
    }

    public MongoClient getClient() {
        return new MongoClient(getUri());
    }

    public String getDatabaseName() {
        return databaseName;
    }
}
