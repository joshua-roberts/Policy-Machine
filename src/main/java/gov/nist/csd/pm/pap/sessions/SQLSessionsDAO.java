package gov.nist.csd.pm.pap.sessions;

import gov.nist.csd.pm.common.exceptions.DatabaseException;
import gov.nist.csd.pm.common.exceptions.SessionDoesNotExistException;
import gov.nist.csd.pm.pap.db.sql.SQLConnection;
import gov.nist.csd.pm.pap.loader.sessions.SQLSessionsLoader;
import gov.nist.csd.pm.pap.loader.sessions.SessionsLoader;
import gov.nist.csd.pm.pap.db.DatabaseContext;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import static gov.nist.csd.pm.common.exceptions.ErrorCodes.ERR_DB;

public class SQLSessionsDAO implements SessionsDAO {

    /**
     * HashMap to store session and User IDs.
     */
    protected HashMap<String, Long> sessions;

    /**
     * Store the connection to mysql.
     */
    private   SQLConnection         conn;

    public SQLSessionsDAO(DatabaseContext ctx) throws DatabaseException {
        conn = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword(), ctx.getSchema());

        //load any sessions already in the database into memory
        SessionsLoader loader = new SQLSessionsLoader(ctx);
        sessions = loader.loadSessions();
    }


    @Override
    public void createSession(String sessionID, long userID) throws DatabaseException {
        try(Statement stmt = conn.getConnection().createStatement()) {
            String sql = "INSERT INTO session(session_id,user_node_id) VALUES ('" + sessionID + "'," + userID +")";
            stmt.execute(sql);
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
        sessions.put(sessionID, userID);
    }

    @Override
    public void deleteSession(String sessionID) throws DatabaseException {
        try(Statement stmt = conn.getConnection().createStatement()) {
            String sql = "DELETE FROM session WHERE session_id = " + sessionID;
            stmt.executeUpdate(sql);
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
        sessions.remove(sessionID);
    }

    @Override
    public long getSessionUserID(String sessionID) throws SessionDoesNotExistException {
        long userID = sessions.get(sessionID);
        if(userID == 0) {
            throw new SessionDoesNotExistException(sessionID);
        }
        return userID;
    }
}
