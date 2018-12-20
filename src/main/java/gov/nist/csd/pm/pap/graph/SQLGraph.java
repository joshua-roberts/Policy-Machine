package gov.nist.csd.pm.pap.graph;

import gov.nist.csd.pm.common.exceptions.Errors;
import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pap.db.sql.SQLConnection;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.db.sql.SQLHelper;

import java.sql.*;
import java.util.*;

import static gov.nist.csd.pm.common.exceptions.Errors.ERR_DB;

public class SQLGraph implements Graph {

    private SQLConnection conn;

    public SQLGraph(DatabaseContext ctx) throws PMException {
        conn = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword(), ctx.getSchema());
    }


    @Override
    public long createNode(Node node) throws PMException {
        if (node == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "a null node was provided when updating a node in mysql");
        }

        //get the ID of the node context, can be 0
        long id = node.getID();

        try(
                CallableStatement cs = conn.getConnection().prepareCall("{? = call create_node_fun(?,?,?)}")
        ) {
            cs.registerOutParameter(1, Types.INTEGER);
            cs.setLong(2, node.getID());
            cs.setString(3, node.getName());
            cs.setString(4, node.getType().toString().toLowerCase());
            cs.execute();

            if (id == 0) {
                id = cs.getInt(1);
            }
        }catch(SQLException e){
            throw new PMException(ERR_DB, e.getMessage());
        }

        //add properties to the node
        if (node.getProperties() != null && !node.getProperties().isEmpty()) {
            String sql = "insert into node_property (property_node_id, property_key, property_value) values ";
            for (String key : node.getProperties().keySet()) {
                sql += String.format("(%d, '%s', '%s')", id, key, node.getProperties().get(key));
            }
            try (Statement stmt = conn.getConnection().createStatement()) {
                stmt.executeUpdate(sql);
            }
            catch (SQLException e) {
                throw new PMException(ERR_DB, e.getMessage());
            }
        }

        //return the ID
        return id;
    }

    @Override
    public void updateNode(Node node) throws PMException {
        if(node == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "a null node was provided when updating a node in neo4j");
        } else if(node.getID() == 0) {
            //throw an exception if the provided context does not have an ID
            throw new PMException(Errors.ERR_NO_ID, "no ID was provided when updating a node in mysql");
        }
        try {
            if(node.getName() != null && !node.getName().isEmpty()) {
                String sql = String.format("update node set name='%s' where node_id = %d", node.getName(), node.getID());
                Statement stmt = conn.getConnection().createStatement();
                stmt.execute(sql);
            }

            if(node.getProperties() != null && !node.getProperties().isEmpty()) {
                // first delete the properties that already exist
                try (Statement stmt = conn.getConnection().createStatement()) {
                    String sql = String.format("delete from node_property where property_node_id=%d", node.getID());
                    stmt.executeUpdate(sql);
                }
                catch (SQLException e) {
                    throw new PMException(ERR_DB, e.getMessage());
                }

                //insert new properties
                try (Statement stmt = conn.getConnection().createStatement()) {
                    String sql = "insert into node_property (property_node_id, property_key, property_value) values ";
                    for (String key : node.getProperties().keySet()) {
                        sql += String.format("(%d, '%s', '%s')", node.getID(), key, node.getProperties().get(key));
                    }
                    stmt.executeUpdate(sql);
                }
                catch (SQLException e) {
                    throw new PMException(ERR_DB, e.getMessage());
                }
            }
        }catch(SQLException e){
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public void deleteNode(long nodeID) throws PMException {
        try(Statement stmt = conn.getConnection().createStatement()) {
            String sql = String.format("delete from node where node_id=%d", nodeID);
            stmt.executeUpdate(sql);
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public boolean exists(long nodeID) throws PMException {
        String sql = String.format("select count(*) from node where node_id=%d", nodeID);
        try(Statement stmt = conn.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            //return if the count of nodes with this ID is 1
            return rs.getInt(1) == 1;
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public HashSet<Node> getNodes() throws PMException {
        try (
                Statement stmt = conn.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery("select node_id from node")
        ){
            HashSet<Node> nodes = new HashSet<>();
            while (rs.next()) {
                long id = rs.getInt(1);
                nodes.add(getNode(id));
            }
            return nodes;
        }catch(SQLException e){
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    private Node getNode(long id) throws PMException {
        try (
                Statement stmt = conn.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery("select node_id,name,node_type_id from node where node_id="+id);
        ) {
            rs.next();
            String name = rs.getString(2);
            NodeType type = SQLHelper.toNodeType(rs.getInt(3));
            HashMap<String, String> properties = getNodeProps(id);

            return new Node(id, name, type, properties);
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    private HashMap<String, String> getNodeProps(long nodeID) throws PMException {
        try (
                Statement stmt = conn.getConnection().createStatement();
                ResultSet propRs = stmt.executeQuery("SELECT property_key, NODE_PROPERTY.property_value FROM NODE_PROPERTY WHERE PROPERTY_NODE_ID = " + nodeID);
        ) {
            HashMap<String, String> props = new HashMap<>();
            while(propRs.next()){
                String key = propRs.getString(1);
                String value = propRs.getString(2);
                props.put(key, value);
            }
            return props;

        } catch(SQLException e){
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public HashSet<Long> getPolicies() throws PMException {
        String sql = String.format("select node_id from nodes where node_type_id=%d", SQLHelper.PC_ID);
        try (
                Statement stmt = conn.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql);
        ) {
            HashSet<Long> pcs = new HashSet<>();
            while(rs.next()){
                pcs.add(rs.getLong(1));
            }
            return pcs;

        } catch(SQLException e){
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public HashSet<Node> getChildren(long nodeID) throws PMException {
        String sql = String.format("select end_node_id from assignments where start_node_id=%d and depth=1", nodeID);
        try (
                Statement stmt = conn.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ){
            HashSet<Node> nodes = new HashSet<>();
            while (rs.next()) {
                long id = rs.getInt(1);
                nodes.add(getNode(id));
            }
            return nodes;
        }catch(SQLException e){
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public HashSet<Node> getParents(long nodeID) throws PMException {
        String sql = String.format("select start_node_id from assignments where end_node_id=%d and depth=1", nodeID);
        try (
                Statement stmt = conn.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ){
            HashSet<Node> nodes = new HashSet<>();
            while (rs.next()) {
                long id = rs.getInt(1);
                nodes.add(getNode(id));
            }
            return nodes;
        }catch(SQLException e){
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public void assign(long childID, NodeType childType, long parentID, NodeType parentType) throws PMException {
        try (CallableStatement stmt = conn.getConnection().prepareCall("{call create_assignment(?,?,?)}")) {
            stmt.setLong(1, childID);
            stmt.setLong(2, parentID);
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.execute();
            String errorMsg = stmt.getString(3);
            if (errorMsg!= null && errorMsg.length() > 0) {
                throw new PMException(ERR_DB, errorMsg);
            }
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public void deassign(long childID, NodeType childType, long parentID, NodeType parentType) throws PMException {
        try {
            CallableStatement stmt = conn.getConnection().prepareCall("{call delete_assignment(?,?,?)}");

            stmt.setLong(1, childID);
            stmt.setLong(2, parentID);
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.execute();
            String errorMsg = stmt.getString(3);
            if (errorMsg!= null && errorMsg.length() > 0) {
                throw new PMException(ERR_DB, errorMsg);
            }
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public void associate(long uaID, long targetID, NodeType targetType, HashSet<String> operations) throws PMException {
        String ops = "";
        for (String op : operations) {
            ops += op + ",";
        }
        ops = ops.substring(0, ops.length() - 1);

        //if an association does not already exist create a new one.  Update the operations
        //if an association does already exist between the two nodes
        boolean associated;
        String sql = String.format("select count(*) from association where ua_id=%d && target_id=%d", uaID, targetID);
        try(Statement stmt = conn.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            //associated is true if associations between these nodes is 1
            associated = rs.getInt(1) == 1;
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }

        if(associated) {
            try (
                    CallableStatement stmt = conn.getConnection().prepareCall("{call create_association(?,?,?,?)}");
            ) {
                stmt.setLong(1, uaID);
                stmt.setLong(2, targetID);
                stmt.setString(3, ops);
                stmt.registerOutParameter(4, Types.VARCHAR);
                stmt.execute();
                String errorMsg = stmt.getString(4);
                if (errorMsg != null && errorMsg.length() > 0) {
                    throw new PMException(ERR_DB, errorMsg);
                }
            }
            catch (SQLException e) {
                throw new PMException(ERR_DB, e.getMessage());
            }
        } else {
            try {
                CallableStatement stmt = conn.getConnection().prepareCall("{? = call update_opset(?,?,?)}");
                stmt.registerOutParameter(1, Types.INTEGER);
                stmt.setLong(2, uaID);
                stmt.setLong(3, targetID);
                stmt.setString(4, ops);
                stmt.execute();
            }
            catch (SQLException e) {
                throw new PMException(ERR_DB, e.getMessage());
            }
        }
    }

    @Override
    public void dissociate(long uaID, long targetID, NodeType targetType) throws PMException {
        try (
                CallableStatement stmt = conn.getConnection().prepareCall("{call delete_association(?,?,?)}")
        ) {
            stmt.setLong(1, uaID);
            stmt.setLong(2, targetID);
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.execute();
            String errorMsg = stmt.getString(3);
            if (errorMsg!= null && errorMsg.length() > 0) {
                throw new PMException(ERR_DB, errorMsg);
            }
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public HashMap<Long, HashSet<String>> getSourceAssociations(long sourceID)  {
        return null;
    }

    @Override
    public HashMap<Long, HashSet<String>> getTargetAssociations(long targetID)  {
        return null;
    }
}
