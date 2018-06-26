package gov.nist.policyserver.dao.neo4j;

import gov.nist.policyserver.dao.SessionsDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.SessionDoesNotExistException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import static gov.nist.policyserver.dao.neo4j.Neo4jHelper.execute;

public class Neo4jSessionsDAO implements SessionsDAO {

    private HashMap<String, Long> sessions = new HashMap<>();
    private Connection            connection;

    public Neo4jSessionsDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createSession(String sessionId, long userId) throws DatabaseException, SQLException {
        String cypher = "match(n:sessions) return n";
        ResultSet rs = execute(connection, cypher);
        if(!rs.next()) {
            cypher = "create(:sessions)";
            execute(connection, cypher);
        }


        cypher = "match(n:sessions) create(n)<-[:session]-(m:session{session_id:'" + sessionId + "', user_id: " + userId + "})";
        execute(connection, cypher);

        sessions.put(sessionId, userId);
    }

    @Override
    public void deleteSession(String sessionId) throws DatabaseException {
        String cypher = "match(n:session{session_id:'" + sessionId + "'}) detach delete n";
        execute(connection, cypher);

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
