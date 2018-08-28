package gov.nist.csd.pm.pip.sql;

import gov.nist.csd.pm.model.exceptions.SessionDoesNotExistException;
import gov.nist.csd.pm.pip.SessionsDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class SqlSessionsDAO implements SessionsDAO {

    private Connection conn;
    private HashMap<String, Long> sessions = new HashMap<>();
    public SqlSessionsDAO(Connection connection) {
        this.conn = connection;
    }

    public void createSession(String sessionId, long userId) throws SQLException {
        String sql = "INSERT INTO session(session_id,user_node_id) VALUES ('" + sessionId + "'," + userId +")";
        Statement stmt = conn.createStatement();
        stmt.execute(sql);
        sessions.put(sessionId, userId);
    }

    @Override
    public void deleteSession(String sessionId) throws SQLException {
        String sql = "DELETE FROM session WHERE session_id = " + sessionId;
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        sessions.remove(sessionId);
    }

    @Override
    public long getSessionUserId(String sessionId) throws SessionDoesNotExistException {
        Long nodeId = sessions.get(sessionId);
        if(nodeId == null) {
            throw new SessionDoesNotExistException(sessionId);
        }

        return nodeId;
    }

}
