package gov.nist.csd.pm.pap.loader.sessions;

import gov.nist.csd.pm.common.exceptions.DatabaseException;
import gov.nist.csd.pm.pap.db.neo4j.Neo4jConnection;
import gov.nist.csd.pm.pap.db.DatabaseContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import static gov.nist.csd.pm.common.exceptions.ErrorCodes.ERR_DB;

/**
 * Neo4j implementation of the SessionsLoader interface
 */
public class Neo4jSessionsLoader implements SessionsLoader {

    /**
     * Store the connection to Neo4j instance.
     */
    protected Neo4jConnection neo4j;

    /**
     * Create a new SessionsLoader from Neo4j, using the provided database connection parameters.
     * @param ctx The parameters to connect to the database
     * @throws DatabaseException If a connection cannot be made to the database
     */
    public Neo4jSessionsLoader(DatabaseContext ctx) throws DatabaseException {
        neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
    }

    /**
     * Load any session in the database into a HashMap.  This map will contain the session IDs as keys, and the User ID
     * that belongs to each session as the values.
     * @return The map of sessionIDs to User IDs.
     * @throws DatabaseException If there is an exception loading sessions from the database.
     */
    @Override
    public HashMap<String, Long> loadSessions() throws DatabaseException {
        HashMap<String, Long> sessions = new HashMap<>();
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

            return sessions;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }
}
