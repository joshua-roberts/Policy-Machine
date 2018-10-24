package gov.nist.csd.pm.pip.sessions;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.SessionDoesNotExistException;
import gov.nist.csd.pm.pip.db.neo4j.Neo4jConnection;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_DB;

public class Neo4jSessionsDAO implements SessionsDAO {
    private HashMap<String, Long> sessions = new HashMap<>();
    private Neo4jConnection       neo4j;

    public Neo4jSessionsDAO(DatabaseContext ctx) throws DatabaseException {
        neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
        loadSessions();
    }

    public void loadSessions() throws DatabaseException {
        sessions.clear();

        String cypher = "match(n:session) return n.user_id, n.session_id";
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                long userId = rs.getLong(1);
                String sessionId = rs.getString(2);

                sessions.put(sessionId, userId);
            }
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public void createSession(String sessionId, long userId) throws DatabaseException {
        String cypher = "merge(n:sessions) create (n)<-[:session{}]-(m:session{session_id:'" + sessionId + "', user_id: " + userId + "})";
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher)
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }

        sessions.put(sessionId, userId);
    }

    @Override
    public void deleteSession(String sessionId) throws DatabaseException {
        String cypher = "match(n:session{session_id:'" + sessionId + "'}) detach delete n";
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher)
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }

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
