package gov.nist.csd.pm.pap.loader.graph;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.graph.relationships.NGACAssignment;
import gov.nist.csd.pm.model.graph.relationships.NGACAssociation;
import gov.nist.csd.pm.pap.db.neo4j.Neo4jConnection;
import gov.nist.csd.pm.pap.db.neo4j.Neo4jHelper;
import gov.nist.csd.pm.pap.db.DatabaseContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_DB;

/**
 * A Neo4j implementation of the GraphLoader interface.
 */
public class Neo4jGraphLoader implements GraphLoader {

    /**
     * Object to hold connection to Neo4j instance.
     */
    protected Neo4jConnection neo4j;

    /**
     * Create a new GraphLoader from Neo4j, using the provided database connection parameters.
     * @param ctx The parameters to connect to the database
     * @throws DatabaseException If a connection cannot be made to the database
     */
    public Neo4jGraphLoader(DatabaseContext ctx) throws DatabaseException {
        neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
    }

    @Override
    public HashSet<Long> getPolicies() throws DatabaseException {
        String cypher = "match(n) where n:PC return n.id";
        return getNodeIDs(cypher);
    }

    @Override
    public HashSet<Long> getNodes() throws DatabaseException {
        String cypher = "match(n) where n:PC or n:OA or n:O or n:UA or n:U return n.id";
        return getNodeIDs(cypher);
    }

    /**
     * Given a cypher query, return all the node IDs that are returned from the query.
     * @param cypher The neo4j cypher query to execute.
     * @return The set of all nodes' IDs
     * @throws DatabaseException When there is an error loading the nodes from the database.
     */
    private HashSet<Long> getNodeIDs(String cypher) throws DatabaseException {
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            HashSet<Long> nodeIDs = new HashSet<>();
            while (rs.next()) {
                nodeIDs.add(rs.getLong(1));
            }
            return nodeIDs;
        } catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public HashSet<NGACAssignment> getAssignments() throws DatabaseException {
        String cypher = "match(n)-[r:assigned_to]->(m) return n.id, m.id";
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            HashSet<NGACAssignment> assignments = new HashSet<>();
            while (rs.next()) {
                assignments.add(new NGACAssignment(rs.getLong(1), rs.getLong(2)));
            }
            return assignments;
        } catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public HashSet<NGACAssociation> getAssociations() throws DatabaseException {
        String cypher = "match(ua:UA)-[a:associated_with]->(oa:OA) return ua.id,oa.id,a.operations";
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            HashSet<NGACAssociation> associations = new HashSet<>();
            while (rs.next()) {
                HashSet<String> ops = Neo4jHelper.getStringSetFromJson(rs.getString(3));
                associations.add(new NGACAssociation(rs.getLong(1), rs.getLong(2), ops));
            }
            return associations;
        }  catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }
}
