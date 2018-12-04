package gov.nist.csd.pm.demos.ndac.pep;

public class TranslateRequest {
    private String sql;
    private String session;
    private long process;
    private String host;
    private int    port;
    private String dbUsername;
    private String dbPassword;
    private String database;

    public TranslateRequest() {

    }

    public TranslateRequest(String sql, String username, long process, String host, int port, String dbUsername, String dbPassword, String database) {
        this.sql = sql;
        this.session = username;
        this.process = process;
        this.host = host;
        this.port = port;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.database = database;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String username) {
        this.session = username;
    }

    public long getProcess() {
        return process;
    }

    public void setProcess(long process) {
        this.process = process;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
}
