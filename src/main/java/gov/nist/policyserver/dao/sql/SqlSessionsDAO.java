package gov.nist.policyserver.dao.sql;

import gov.nist.policyserver.dao.SessionsDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.SessionDoesNotExistException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import static gov.nist.policyserver.dao.neo4j.Neo4jHelper.execute;

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
