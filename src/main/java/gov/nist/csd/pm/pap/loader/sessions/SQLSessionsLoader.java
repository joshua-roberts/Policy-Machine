package gov.nist.csd.pm.pap.loader.sessions;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.pap.db.sql.SQLConnection;
import gov.nist.csd.pm.pap.db.DatabaseContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_DB;

/**
 * MySQL implementation of te SessionsLoader interface
 */
public class SQLSessionsLoader implements SessionsLoader {

    /**
     * Store the connection to mysql.
     */
    private SQLConnection conn;

    public SQLSessionsLoader(DatabaseContext ctx) throws DatabaseException {
        conn = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword(), ctx.getSchema());
    }

    /**
     * Load the sessions in the database into a HashMap.  The map stores sessionIDs that point to the ID of the corresponding
     * User.
     * @return A map of sessionIDs to User IDs.
     * @throws DatabaseException If there is an error loading the sessions into memory from the database.
     */
    @Override
    public HashMap<String, Long> loadSessions() throws DatabaseException {
        HashMap<String, Long> sessions = new HashMap<>();
        String sql = "select session_id, user_node_id from sessions";
        try(Statement stmt = conn.getConnection().createStatement();
            ResultSet rs   = stmt.executeQuery(sql)) {
            while(rs.next()) {
                sessions.put(rs.getString(1), rs.getLong(2));
            }

            return sessions;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }
}
