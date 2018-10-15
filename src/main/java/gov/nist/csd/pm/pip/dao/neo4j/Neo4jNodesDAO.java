package gov.nist.csd.pm.pip.dao.neo4j;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.pep.response.ApiResponseCodes;
import gov.nist.csd.pm.model.graph.NodeType;
import gov.nist.csd.pm.pip.dao.NodesDAO;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static gov.nist.csd.pm.model.Constants.NEW_NODE_ID;

public class Neo4jNodesDAO implements NodesDAO {

    private Neo4jConnection neo4j;

    public Neo4jNodesDAO(DatabaseContext ctx) throws DatabaseException {
        neo4j = new Neo4jConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
    }

    @Override
    public Node createNode(long id, String name, NodeType type, Map<String, String> properties) throws DatabaseException {
        String cypher;

        if (id == NEW_NODE_ID) {
            cypher = String.format("match(n) with max(n.id) as ID create (n:NODE:%s{id: ID+1, name: '%s', type: '%s'})", type, name, type);
        } else {
            cypher = String.format("create (n:NODE:%s{id: %d, name: '%s', type: '%s'})", type, id, name, type);
        }

        String propStr = "";
        for (String key : properties.keySet()) {
            if (propStr.length() == 0) {
                propStr += String.format("%s: '%s'", key, properties.get(key));
            }
            else {
                propStr += String.format(", %s: '%s'", key, properties.get(key));
            }
        }
        cypher += String.format(" set n += {%s} return n.id", propStr);

        ResultSet rs = neo4j.execute(cypher);
        try {
            rs.next();
            id = rs.getLong(1);
        }
        catch (SQLException e) {
            throw new DatabaseException(ApiResponseCodes.ERR_NEO, "error returning the new node's ID");
        }

        return new Node(id, name, type, properties);
    }

    @Override
    public void updateNode(long nodeID, String name) throws DatabaseException {
        if(name != null && !name.isEmpty()) {
            String cypher = String.format("merge (n {id: %d}) set n.name='%s'", nodeID, name);
            neo4j.execute(cypher);
        }
    }

    @Override
    public void deleteNode(long nodeID) throws DatabaseException {
        String cypher = String.format("MATCH (n) where n.id=%d DETACH DELETE n", nodeID);
        neo4j.execute(cypher);
    }

    @Override
    public void addNodeProperty(long nodeID, String key, String value) throws DatabaseException {
        String cypher = String.format("match(n{id: %d}) set n.%s='%s'", nodeID, key, value);
        neo4j.execute(cypher);
    }

    @Override
    public void deleteNodeProperty(long nodeID, String key) throws DatabaseException {
        String cypher = String.format("match(n{id: %d}) remove n.%s", nodeID, key);
        neo4j.execute(cypher);
    }

    @Override
    public void updateNodeProperty(long nodeID, String key, String value) throws DatabaseException {
        String cypher = String.format("match(n{id: %d}) set n.%s = '%s'", nodeID, key, value);
        neo4j.execute(cypher);
    }
}
