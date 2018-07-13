package gov.nist.policyserver.dao.sql;

import gov.nist.policyserver.dao.NodesDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.InvalidNodeTypeException;
import gov.nist.policyserver.exceptions.InvalidPropertyException;
import gov.nist.policyserver.helpers.JsonHelper;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.nodes.Property;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static gov.nist.policyserver.common.Constants.ERR_NEO;
import static gov.nist.policyserver.dao.neo4j.Neo4jHelper.execute;

public class SqlNodesDAO implements NodesDAO {

    private Connection conn;

    public SqlNodesDAO(Connection connection) {
        this.conn = connection;
    }

    public List<Node> getNodes() throws DatabaseException {
        try {
            List<Node> nodes = new ArrayList<>();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select node_id,name,node_type_id,description from node");
            while (rs.next()) {
                long id = rs.getInt(1);
                String name = rs.getString(2);
                NodeType type = NodeType.toNodeType(rs.getInt(3));
                String description = rs.getString(4);
                Node node = new Node(id, name, type, description);

                Statement propStmt = conn.createStatement();
                List<Property> props = getNodeProps(node);
                for(int i=0; i < props.size();i++){
                    node.addProperty(props.get(i));
                }

                nodes.add(node);
            }
            return nodes;
        }catch(SQLException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }catch(InvalidNodeTypeException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    public List<Property> getNodeProps(Node node) throws DatabaseException {
        try {
            List<Property> props = new ArrayList<>();
            Statement stmt = conn.createStatement();
            ResultSet propRs = stmt.executeQuery("SELECT property_key, NODE_PROPERTY.property_value FROM NODE_PROPERTY WHERE PROPERTY_NODE_ID = " + node.getId());
            while(propRs.next()){
                String key = propRs.getString(1);
                String value = propRs.getString(2);
                Property prop = new Property(key, value);
                props.add(prop);
            }
            return props;

    }catch(SQLException e){
        throw new DatabaseException(e.getErrorCode(), e.getMessage());
    }catch(InvalidPropertyException e){
        throw new DatabaseException(e.getErrorCode(), e.getMessage());
    }
    }

    @Override
    public Node createNode(long id, String name, NodeType type) throws DatabaseException {
        try{
            CallableStatement cs = conn.prepareCall("{? = call create_node_fun(?,?,?)}");
            cs.registerOutParameter(1, Types.INTEGER);
            cs.setLong(2, id);
            cs.setString(3, name);
            cs.setString(4, type.name());

            cs.execute();
            if (id == 0) {
                id = cs.getInt(1);
            }

            return new Node(id, name, type);
        }catch(SQLException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public synchronized void updateNode(long nodeId, String name) throws DatabaseException {
        try {
            if(name != null && !name.isEmpty()) {
                String sql = "update node set name='" + name + "' where node_id = " + nodeId  ;
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
            }
        }catch(SQLException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public synchronized void deleteNode(long nodeId) throws DatabaseException {
        try{
            String sql = "delete from node where node_id=" + nodeId;
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public synchronized void addNodeProperty(long nodeId, Property property) throws DatabaseException {
        try{
            String sql = "insert into node_property (property_node_id, property_key, property_value) values (" + nodeId + ", '" + property.getKey() + "', '" + property.getValue() + "')";
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public synchronized void deleteNodeProperty(long nodeId, String key) throws DatabaseException {
        int deletedRows = 0;
        try{
            String sql = "delete from node_property where property_node_id=" + nodeId + " and property_key='" + key + "'";
            Statement stmt = conn.createStatement();
            deletedRows = stmt.executeUpdate(sql);
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public void updateNodeProperty(long nodeId, String key, String value) {

    }
}
