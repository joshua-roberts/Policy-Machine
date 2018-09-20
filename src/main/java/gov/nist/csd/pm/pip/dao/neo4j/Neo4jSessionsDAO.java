package gov.nist.csd.pm.pip.dao.neo4j;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.SessionDoesNotExistException;
import gov.nist.csd.pm.pip.dao.SessionsDAO;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class Neo4jSessionsDAO implements SessionsDAO {

    private HashMap<String, Long> sessions = new HashMap<>();
    private Neo4jConnection       neo4j;

    public Neo4jSessionsDAO(DatabaseContext ctx) throws DatabaseException, SQLException {
        neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
        loadSessions();
    }

    public void loadSessions() throws DatabaseException, SQLException {
        sessions.clear();

        String cypher = "match(n:session) return n.user_id, n.session_id";
        ResultSet rs = neo4j.execute(cypher);
        while(rs.next()) {
            long userId = rs.getLong(1);
            String sessionId = rs.getString(2);

            sessions.put(sessionId, userId);
        }
    }

    @Override
    public void createSession(String sessionId, long userId) throws DatabaseException, SQLException {
        String cypher = "match(n:sessions) return n";
        ResultSet rs = neo4j.execute(cypher);
        if(!rs.next()) {
            cypher = "create(:sessions)";
            neo4j.execute(cypher);
        }


        cypher = "match(n:sessions) create(n)<-[:session]-(m:session{session_id:'" + sessionId + "', user_id: " + userId + "})";
        neo4j.execute(cypher);

        sessions.put(sessionId, userId);
    }

    @Override
    public void deleteSession(String sessionId) throws DatabaseException {
        String cypher = "match(n:session{session_id:'" + sessionId + "'}) detach delete n";
        neo4j.execute(cypher);

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
