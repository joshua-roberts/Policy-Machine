package gov.nist.csd.pm.pap.search;

import gov.nist.csd.pm.common.exceptions.DatabaseException;
import gov.nist.csd.pm.common.exceptions.InvalidNodeTypeException;
import gov.nist.csd.pm.common.exceptions.NodeNotFoundException;
import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.pap.db.neo4j.Neo4jConnection;
import gov.nist.csd.pm.pap.db.neo4j.Neo4jHelper;
import gov.nist.csd.pm.pap.db.DatabaseContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static gov.nist.csd.pm.common.exceptions.ErrorCodes.ERR_DB;

/**
 * Neo4j extension of the Search class.
 */
public class Neo4jSearch implements Search {

    /**
     * Object to hold connection to Neo4j instance.
     */
    protected Neo4jConnection neo4j;

    /**
     * Create a new Search with the given Neo4j connection context.
     */
    public Neo4jSearch(DatabaseContext ctx) throws DatabaseException {
        this.neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
    }

    /**
     * Create a new Search with the given Neo4j connection.
     */
    public Neo4jSearch(Neo4jConnection neo4j) {
        this.neo4j = neo4j;
    }

    @Override
    public HashSet<Node> search(String name, String type, Map<String, String> properties) throws DatabaseException {
        // get the cypher query
        String cypher = getSearchCypher(name, type, properties);
        // query neo4j for the nodes
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            return Neo4jHelper.getNodesFromResultSet(rs);
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public Node getNode(long id) throws NodeNotFoundException, DatabaseException, InvalidNodeTypeException {
        // get the cypher query
        String cypher = String.format("match(n{id:%d}) return n", id);
        // query neo4j for the nodes
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            if(rs.next()) {
                LinkedHashMap map = (LinkedHashMap) rs.getObject(1);
                return Neo4jHelper.mapToNode(map);
            }

            throw new NodeNotFoundException(id);
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    /**
     * Build the cypher query according to the given parameters.
     */
    protected static String getSearchCypher(String name, String type, Map<String,String> properties) {
        String propsStr = "";
        if (name != null && !name.isEmpty()){
            propsStr = String.format("name: \"%s\"", name);
        }

        String typeStr = "";
        if (type != null && !type.isEmpty()){
            typeStr = String.format(":%s", type);
        }

        if (properties != null) {
            for (String key : properties.keySet()) {
                String value = properties.get(key);
                if (propsStr.isEmpty()) {
                    propsStr += String.format("%s: \"%s\"", key, value);
                } else {
                    propsStr += String.format(", %s: \"%s\"", key, value);
                }
            }
        }

        return String.format("match(n%s{%s}) return n", typeStr, propsStr);
    }
}
