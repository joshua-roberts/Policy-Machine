package gov.nist.csd.pm.pap.search;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.pap.db.neo4j.Neo4jConnection;
import gov.nist.csd.pm.pap.db.neo4j.Neo4jHelper;
import gov.nist.csd.pm.pap.db.DatabaseContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static gov.nist.csd.pm.common.exceptions.Errors.ERR_DB;
import static gov.nist.csd.pm.common.exceptions.Errors.ERR_NODE_NOT_FOUND;

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
    public Neo4jSearch(DatabaseContext ctx) throws PMException {
        this.neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
    }

    /**
     * Create a new Search with the given Neo4j connection.
     */
    public Neo4jSearch(Neo4jConnection neo4j) {
        this.neo4j = neo4j;
    }

    @Override
    public HashSet<NodeContext> search(String name, String type, Map<String, String> properties) throws PMException {
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
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public NodeContext getNode(long id) throws PMException {
        // get the cypher query
        String cypher = String.format("match(n{id:%d}) return n", id);
        // query neo4j for the nodes
        try(
                Connection conn = neo4j.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cypher);
                ResultSet rs = stmt.executeQuery()
        ) {
            if(rs.next()) {
                HashMap map = (HashMap) rs.getObject(1);
                return Neo4jHelper.mapToNode(map);
            }

            throw new PMException(ERR_NODE_NOT_FOUND, String.format("node with ID %d does not exist", id));
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
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
