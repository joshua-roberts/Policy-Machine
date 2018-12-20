package gov.nist.csd.pm.pap.db.sql;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.pap.db.DatabaseContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static gov.nist.csd.pm.common.exceptions.Errors.ERR_DB;

/**
 * Store a connection to a MySQL database.
 */
public class SQLConnection {

    private Connection connection;

    public SQLConnection(String host, int port, String username, String password, String db) throws PMException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, username, password);
        } catch (SQLException | ClassNotFoundException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    /**
     * Utility method to create a SQLConnection instance using the provided DatabaseContext
     * @param ctx The context to create the SQLConnection from
     * @return A SQLConnection instance
     */
    public static SQLConnection fromCtx(DatabaseContext ctx) throws PMException {
        return new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword(), ctx.getSchema());
    }

    public Connection getConnection() {
        return connection;
    }
}
