package gov.nist.csd.pm.pap.db;

/**
 * Class to hold information about a database connection.  This can be used for Neo4j and MySQL. The schema field
 * will be ignored for Neo4j.
 */
public class DatabaseContext {

    public static enum DATABASES {
        NEO4J,
        MYSQL
    }

    public static DATABASES toEnum(String database) {
        switch (database.toUpperCase()) {
            case "NEO4J":
                return DATABASES.NEO4J;
            case "MYSQL":
                return DATABASES.MYSQL;
            default:
                return null;
        }
    }

    DATABASES database;
    private String host;
    private int port;
    private String username;
    private String password;
    private String schema;

    public DatabaseContext(String host, int port, String username, String password, String schema) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.schema = schema;
    }

    public DatabaseContext(DATABASES database, String host, int port, String username, String password, String schema) {
        this.database = database;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.schema = schema;
    }

    public DATABASES getDatabase() {
        return database;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSchema() {
        return schema;
    }
}
