package gov.nist.csd.pm.pap.sessions;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.pap.db.neo4j.Neo4jConnection;
import gov.nist.csd.pm.pap.db.DatabaseContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static gov.nist.csd.pm.common.exceptions.Errors.ERR_DB;

public class Neo4jSessionsDAO implements SessionsDAO {

    /**
     * Object holding the connection to the neo4j instance.
     */
    private Neo4jConnection       neo4j;

    public Neo4jSessionsDAO(DatabaseContext ctx) throws PMException {
        neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
    }

    @Override
    public void createSession(String sessionId, long userId) throws PMException {
        String cypher = "merge(n:sessions) create (n)<-[:session{}]-(m:session{sessionID:'" + sessionId + "', userID: " + userId + "})";
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher)
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
        }

    @Override
    public void deleteSession(String sessionID) throws PMException {
        String cypher = "match(n:session{sessionID:'" + sessionID + "'}) detach delete n";
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher)
        ) {
            stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
        }

    @Override
    public long getSessionUserID(String sessionID) throws PMException {
        String cypher = "match(n:session{sessionID:'" + sessionID + "'}) return n.userID";
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            if(rs.next()) {
                return rs.getLong(1);
            }
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }

        return 0;
    }
}
