package gov.nist.csd.pm.pip.db.sql;

import gov.nist.csd.pm.model.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_DB;

public class SQLConnection {

    private Connection connection;

    public SQLConnection(String host, int port, String username, String password, String db) throws DatabaseException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, username, password);
        } catch (SQLException | ClassNotFoundException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
