package gov.nist.csd.pm.pip.db;

/**
 * Class to hold information about a database connection.  This can be used for Neo4j and MySQL. The schema field
 * will be ignored for Neo4j.
 */
public class DatabaseContext {

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
