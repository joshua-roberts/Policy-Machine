package gov.nist.policyserver.dao.neo4j;

import gov.nist.policyserver.dao.NodesDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.helpers.JsonHelper;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.nodes.Property;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static gov.nist.policyserver.common.Constants.ERR_NEO;
import static gov.nist.policyserver.common.Constants.NEW_NODE_ID;
import static gov.nist.policyserver.dao.neo4j.Neo4jHelper.execute;
import static gov.nist.policyserver.dao.neo4j.Neo4jHelper.getNodesFromResultSet;

public class Neo4jNodesDAO implements NodesDAO {

    private Connection connection;

    public Neo4jNodesDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Node createNode(long id, String name, NodeType type) throws DatabaseException {
        if(id == NEW_NODE_ID) {
            id = getMaxId() + 1;
        }
        String cypher = "CREATE " +
                "(n:" + type +
                "{" +
                "id: " + id + ", " +
                "name:'" + name + "'," +
                "type:'" + type + "'})";
        execute(connection, cypher);

        return new Node(id, name, type);
    }

    public long getMaxId() throws DatabaseException {
        String cypher = "match(n) return max(n.id)";
        try {
            ResultSet rs = execute(connection, cypher);
            rs.next();
            long maxId = rs.getLong(1);
            if(maxId == -1) {
                maxId = 1;
            }
            return maxId;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    @Override
    public void updateNode(long nodeId, String name) throws DatabaseException {
        if(name != null && !name.isEmpty()) {
            //update name
            String cypher = "merge (n {id:" + nodeId + "}) set n.name='" + name + "'";
            execute(connection, cypher);
        }
    }

    @Override
    public void deleteNode(long nodeId) throws DatabaseException {
        //delete node
        String cypher = "MATCH (n) where n.id=" + nodeId + " DETACH DELETE n";
        execute(connection, cypher);
    }

    @Override
    public void addNodeProperty(long nodeId, Property property) throws DatabaseException {
        String cypher = "match(n{id:" + nodeId + "}) set n." + property.getKey() + "='" + property.getValue() + "'";
        execute(connection, cypher);
    }

    @Override
    public void deleteNodeProperty(long nodeId, String key) throws DatabaseException {
        String cypher = "match(n{id:" + nodeId + "}) remove n." + key;
        execute(connection, cypher);
    }

    @Override
    public void updateNodeProperty(long nodeId, String key, String value) throws DatabaseException {
        String cypher = "match(n{id:" + nodeId + "}) set n." + key + " = '" + value + "'";
        execute(connection, cypher);
    }
}
