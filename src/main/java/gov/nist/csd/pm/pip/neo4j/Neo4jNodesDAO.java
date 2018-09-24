package gov.nist.csd.pm.pip.neo4j;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidNodeTypeException;
import gov.nist.csd.pm.pip.NodesDAO;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.nodes.Property;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static gov.nist.csd.pm.model.Constants.ERR_NEO;
import static gov.nist.csd.pm.model.Constants.NEW_NODE_ID;
import static gov.nist.csd.pm.pip.neo4j.Neo4jHelper.execute;

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

    @Override
    public void setNodeProperties(long nodeId, Property[] properties) throws DatabaseException {
        // SET n += { hungry: TRUE , position: 'Entrepreneur' }
        if(properties.length > 0) {
            String cypher = "match(n{id:" + nodeId + "}) set n += {";
            String propStr = "";
            for(Property prop : properties) {
                if(propStr.isEmpty()) {
                    propStr += prop.getKey() + ": '" + prop.getValue() + "'";
                } else {
                    propStr += "," + prop.getKey() + ": '" + prop.getValue() + "'";
                }
            }
            cypher += propStr + "}";
            execute(connection, cypher);
        }
    }

    @Override
    public Node createNode(long id, String name, String type, Property[] properties) throws DatabaseException, InvalidNodeTypeException {
        String propStr = "";
        for(Property prop : properties) {
            propStr += "," + prop.getKey() + ": '" + prop.getValue() + "'";
        }


        String cypher = "CREATE " +
                "(n:" + type +
                "{" +
                "id: " + id + ", " +
                "name:'" + name + "'," +
                "type:'" + type + "'" +
                 propStr + "})";
        execute(connection, cypher);

        Node node = new Node(id, name, NodeType.toNodeType(type), properties);

        return node;
    }
}
