package gov.nist.csd.pm.pip.dao.sql;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.graph.NodeType;
import gov.nist.csd.pm.pip.dao.NodesDAO;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.*;
import java.util.HashMap;

public class SQLNodesDAO implements NodesDAO {

    private SQLConnection mysql;

    public SQLNodesDAO(DatabaseContext ctx) throws DatabaseException {
        mysql = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
    }

    @Override
    public Node createNode(long id, String name, NodeType nodeType, HashMap<String, String> properties) throws DatabaseException {
        try{
            System.out.println("Creating node - calling create_node_fun " + id + "-" + name + "-" + nodeType.toString().toLowerCase());
            CallableStatement cs = mysql.getConnection().prepareCall("{? = call create_node_fun(?,?,?)}");
            cs.registerOutParameter(1, Types.INTEGER);
            cs.setLong(2, id);
            cs.setString(3, name);
            cs.setString(4, nodeType.toString().toLowerCase());

            cs.execute();
            if (id == 0) {
                id = cs.getInt(1);
            }
            for(String key : properties.keySet()) {
                addNodeProperty(id, key, properties.get(key));
            }
            return new Node(id, name, nodeType, properties);
        }catch(SQLException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public synchronized void updateNode(long nodeId, String name) throws DatabaseException {
        try {
            if(name != null && !name.isEmpty()) {
                String sql = "update node set name='" + name + "' where node_id = " + nodeId  ;
                Statement stmt = mysql.getConnection().createStatement();
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
            Statement stmt = mysql.getConnection().createStatement();
            stmt.execute(sql);
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public void addNodeProperty(long nodeId, String key, String value) throws DatabaseException {
        try{
            String sql = "insert into node_property (property_node_id, property_key, property_value) values (" + nodeId + ", '" + key + "', '" + value + "')";
            Statement stmt = mysql.getConnection().createStatement();
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
            Statement stmt = mysql.getConnection().createStatement();
            deletedRows = stmt.executeUpdate(sql);
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public void updateNodeProperty(long nodeId, String key, String value) throws DatabaseException {
        int updatedRows = 0;
        try{
            String sql = "update node_property set property_value = '" + value + "' where property_node_id=" + nodeId + " and property_key='" + key + "'";
            Statement stmt = mysql.getConnection().createStatement();
            updatedRows = stmt.executeUpdate(sql);
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }
}
