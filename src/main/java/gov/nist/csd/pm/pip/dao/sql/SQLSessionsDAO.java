package gov.nist.csd.pm.pip.dao.sql;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.SessionDoesNotExistException;
import gov.nist.csd.pm.pip.dao.SessionsDAO;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class SQLSessionsDAO implements SessionsDAO {

    private HashMap<String, Long> sessions = new HashMap<>();
    private SQLConnection         mysql;

    public SQLSessionsDAO(DatabaseContext ctx) throws DatabaseException {
        mysql = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword(), ctx.getSchema());
    }

    public void createSession(String sessionId, long userId) throws SQLException {
        String sql = "INSERT INTO session(session_id,user_node_id) VALUES ('" + sessionId + "'," + userId +")";
        Statement stmt = mysql.getConnection().createStatement();
        stmt.execute(sql);
        sessions.put(sessionId, userId);
    }

    @Override
    public void deleteSession(String sessionId) throws SQLException {
        String sql = "DELETE FROM session WHERE session_id = " + sessionId;
        Statement stmt = mysql.getConnection().createStatement();
        stmt.executeUpdate(sql);
        sessions.remove(sessionId);
    }

    @Override
    public long getSessionUserID(String sessionID) throws SessionDoesNotExistException {
        Long nodeId = sessions.get(sessionID);
        if(nodeId == null) {
            throw new SessionDoesNotExistException(sessionID);
        }

        return nodeId;
    }
}
