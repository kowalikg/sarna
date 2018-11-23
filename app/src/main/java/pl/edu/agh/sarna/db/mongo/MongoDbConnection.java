package pl.edu.agh.sarna.db.mongo;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.util.Collections;
import java.util.List;
public class MongoDbConnection {

    private String user;
    private String password;
    private String host;
    private int port;
    private String databaseName;
    private MongoClientOptions options;
    private static final String DEFAULT_USER = "student";
    private static final String DEFAULT_PASSWORD = "Student@1234";
    private static final String DEFAULT_HOST = "ds137643.mlab.com";
    private static final int DEFAULT_PORT = 37643;
    private static final String DEFAULT_DATABASE_NAME = "sarna-db";
    private static final int DEFAULT_CONNECTION_TIMEOUT_MILLIS = 10000;
    private static final MongoClientOptions DEFAULT_OPTIONS = getDefaultOptions();

    public MongoDbConnection(String user, String password, String host, int port,
                             String databaseName, MongoClientOptions options) {
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.options = options;
    }
    public static MongoDbConnection getDefaultConnection() {
        return new MongoDbConnection(DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_HOST, DEFAULT_PORT,
                DEFAULT_DATABASE_NAME, DEFAULT_OPTIONS);
    }
    private static MongoClientOptions getDefaultOptions() {
        return MongoClientOptions.builder()
                .serverSelectionTimeout(DEFAULT_CONNECTION_TIMEOUT_MILLIS).build();
    }
    public MongoClientOptions getOptions() {
        return options;
    }

    public MongoCredential getCredential() {
        return MongoCredential.createCredential(user, databaseName, password.toCharArray());
    }

    public List<MongoCredential> getCredentialList() {
        return Collections.singletonList(getCredential());
    }

    public ServerAddress getServerAddress() {
        return new ServerAddress(host, port);
    }

    public String getDatabaseName() {
        return databaseName;
    }
}