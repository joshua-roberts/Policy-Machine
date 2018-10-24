package gov.nist.csd.pm.pip.graph;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.NGAC;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pip.db.sql.SQLConnection;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.*;
import java.util.*;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_DB;

public class NGACSQL implements NGAC {

    private SQLConnection conn;

    public NGACSQL(DatabaseContext ctx) throws DatabaseException {
        conn = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword(), ctx.getSchema());
    }


    @Override
    public Node createNode(Node ctx) throws NullNodeCtxException, DatabaseException {
        if (ctx == null) {
            throw new NullNodeCtxException();
        }

        long id = ctx.getID();

        try(
                CallableStatement cs = conn.getConnection().prepareCall("{? = call create_node_fun(?,?,?)}")
        ) {
            cs.registerOutParameter(1, Types.INTEGER);
            cs.setLong(2, ctx.getID());
            cs.setString(3, ctx.getName());
            cs.setString(4, ctx.getType().toString().toLowerCase());
            cs.execute();

            if (id == 0) {
                id = cs.getInt(1);
            }
        }catch(SQLException e){
            throw new DatabaseException(ERR_DB, e.getMessage());
        }

        //add properties to the node
        if (ctx.getProperties() != null && !ctx.getProperties().isEmpty()) {
            String sql = "insert into node_property (property_node_id, property_key, property_value) values ";
            for (String key : ctx.getProperties().keySet()) {
                sql += String.format("(%d, '%s', '%s')", id, key, ctx.getProperties().get(key));
            }
            try (Statement stmt = conn.getConnection().createStatement()) {
                stmt.executeUpdate(sql);
            }
            catch (SQLException e) {
                throw new DatabaseException(ERR_DB, e.getMessage());
            }
        }

        //return the provided node ctx with the ID
        return ctx.id(id);
    }

    @Override
    public void updateNode(Node ctx) throws NullNodeCtxException, NoIDException, DatabaseException {
        if(ctx == null) {
            throw new NullNodeCtxException();
        } else if(ctx.getID() == 0) {
            //throw an exception if the provided context does not have an ID
            throw new NoIDException();
        }
        try {
            if(ctx.getName() != null && !ctx.getName().isEmpty()) {
                String sql = String.format("update node set name='%s' where node_id = %d", ctx.getName(), ctx.getID());
                Statement stmt = conn.getConnection().createStatement();
                stmt.execute(sql);
            }

            if(ctx.getProperties() != null && !ctx.getProperties().isEmpty()) {
                // first delete the properties that already exist
                try (Statement stmt = conn.getConnection().createStatement()) {
                    String sql = String.format("delete from node_property where property_node_id=%d", ctx.getID());
                    stmt.executeUpdate(sql);
                }
                catch (SQLException e) {
                    throw new DatabaseException(ERR_DB, e.getMessage());
                }

                //insert new properties
                try (Statement stmt = conn.getConnection().createStatement()) {
                    String sql = "insert into node_property (property_node_id, property_key, property_value) values ";
                    for (String key : ctx.getProperties().keySet()) {
                        sql += String.format("(%d, '%s', '%s')", ctx.getID(), key, ctx.getProperties().get(key));
                    }
                    stmt.executeUpdate(sql);
                }
                catch (SQLException e) {
                    throw new DatabaseException(ERR_DB, e.getMessage());
                }
            }
        }catch(SQLException e){
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public void deleteNode(long nodeID) throws DatabaseException {
        try(Statement stmt = conn.getConnection().createStatement()) {
            String sql = String.format("delete from node where node_id=%d", nodeID);
            stmt.executeUpdate(sql);
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public boolean exists(long nodeID) throws DatabaseException {
        String sql = String.format("select count(*) from node where node_id=%d", nodeID);
        try(Statement stmt = conn.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            //return if the count of nodes with this ID is 1
            return rs.getInt(1) == 1;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public HashSet<Node> getNodes() throws DatabaseException {
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
        }catch(SQLException | InvalidNodeTypeException e){
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    private Node getNode(long id) throws SQLException, InvalidNodeTypeException, DatabaseException {
        try (
                Statement stmt = conn.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery("select node_id,name,node_type_id from node where node_id="+id);
        ) {
            rs.next();
            String name = rs.getString(2);
            NodeType type = NodeType.toNodeType(rs.getInt(3));
            HashMap<String, String> properties = getNodeProps(id);

            return new Node(id, name, type, properties);
        }
    }

    private HashMap<String, String> getNodeProps(long nodeID) throws DatabaseException {
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
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public HashSet<Long> getPolicies() throws DatabaseException {
        String sql = String.format("select node_id from nodes where node_type_id=%d", NodeType.PC_ID);
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
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public HashSet<Node> getChildren(long nodeID) throws DatabaseException {
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
        }catch(SQLException | InvalidNodeTypeException e){
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public HashSet<Node> getParents(long nodeID) throws DatabaseException {
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
        }catch(SQLException | InvalidNodeTypeException e){
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public void assign(Node childCtx, Node parentCtx) throws DatabaseException, NullNodeCtxException {
        //check that neither of the ctxs are null to avoid NPE
        if(childCtx == null || parentCtx == null) {
            throw new NullNodeCtxException();
        }

        try (CallableStatement stmt = conn.getConnection().prepareCall("{call create_assignment(?,?,?)}")) {
            stmt.setInt(1, (int) childCtx.getID());
            stmt.setInt(2, (int) parentCtx.getID());
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.execute();
            String errorMsg = stmt.getString(3);
            if (errorMsg!= null && errorMsg.length() > 0) {
                throw new DatabaseException(ERR_DB, errorMsg);
            }
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public void deassign(Node childCtx, Node parentCtx) throws DatabaseException {
        try {
            CallableStatement stmt = conn.getConnection().prepareCall("{call delete_assignment(?,?,?)}");

            stmt.setLong(1, childCtx.getID());
            stmt.setLong(2, parentCtx.getID());
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.execute();
            String errorMsg = stmt.getString(3);
            if (errorMsg!= null && errorMsg.length() > 0) {
                throw new DatabaseException(ERR_DB, errorMsg);
            }
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public void associate(long uaID, long targetID, Collection<String> operations) throws DatabaseException {
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
            throw new DatabaseException(ERR_DB, e.getMessage());
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
                    throw new DatabaseException(ERR_DB, errorMsg);
                }
            }
            catch (SQLException e) {
                throw new DatabaseException(e.getErrorCode(), e.getMessage());
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
                throw new DatabaseException(e.getErrorCode(), e.getMessage());
            }
        }
    }

    @Override
    public void dissociate(long uaID, long targetID) throws DatabaseException {
        try (
            CallableStatement stmt = conn.getConnection().prepareCall("{call delete_association(?,?,?)}")) {
            stmt.setLong(1, uaID);
            stmt.setLong(2, targetID);
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.execute();
            String errorMsg = stmt.getString(3);
            if (errorMsg!= null && errorMsg.length() > 0) {
                throw new DatabaseException(ERR_DB, errorMsg);
            }
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public HashMap<Long, Collection<String>> getSourceAssociations(long sourceID)  {
        return null;
    }

    @Override
    public HashMap<Long, Collection<String>> getTargetAssociations(long targetID)  {
        return null;
    }
}
