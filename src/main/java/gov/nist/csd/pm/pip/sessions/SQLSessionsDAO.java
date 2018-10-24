package gov.nist.csd.pm.pip.sessions;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.SessionDoesNotExistException;
import gov.nist.csd.pm.pip.db.sql.SQLConnection;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_DB;

public class SQLSessionsDAO extends SessionsDAO {
    private SQLConnection         conn;

    public SQLSessionsDAO(DatabaseContext ctx) throws DatabaseException {
        conn = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword(), ctx.getSchema());
        loadSessions();
    }

    @Override
    public void loadSessions() throws DatabaseException {
        sessions.clear();

        String sql = "select session_id, user_node_id from sessions";
        try(Statement stmt = conn.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                sessions.put(rs.getString(1), rs.getLong(2));
            }
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
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
}
