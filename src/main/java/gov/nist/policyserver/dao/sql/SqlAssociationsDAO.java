package gov.nist.policyserver.dao.sql;

import gov.nist.policyserver.dao.AssociationsDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.InvalidNodeTypeException;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.relationships.Association;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class SqlAssociationsDAO implements AssociationsDAO {

    private Connection conn;

    public SqlAssociationsDAO(Connection connection) {
        this.conn = connection;
    }

    public List<Association> getAssociations() throws DatabaseException {
        try{
            List<Association> associations = new ArrayList<>();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ua_id,a.name,a.node_type_id, get_operations(opset_id),oa_id,b.name,b.node_type_id FROM association join node a on ua_id = a.node_id join node b on oa_id=b.node_id");
            while (rs.next()) {
                long id = rs.getInt(1);
                String name = rs.getString(2);
                NodeType type = NodeType.toNodeType(rs.getInt(3));
                Node startNode = new Node(id, name, type);

                HashSet<String> ops = new HashSet<>(Arrays.asList(rs.getString(4).split(",")));

                id = rs.getInt(5);
                name = rs.getString(6);
                type = NodeType.toNodeType(rs.getInt(7));
                Node endNode = new Node(id, name, type);

                associations.add(new Association(startNode, endNode, ops, true));
            }
            return associations;
        }catch(SQLException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }catch(InvalidNodeTypeException e){
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public synchronized void createAssociation(long uaId, long targetId, HashSet<String> operations, boolean inherit) throws DatabaseException {
        String ops = "";
        boolean result;
        for(String op : operations){
            ops += op + ",";
        }
        ops = ops.substring(0, ops.length()-1);
        try {
            System.out.println("Calling Create_Association Procedure");
            CallableStatement stmt = conn.prepareCall("{call create_association(?,?,?,?)}");
            stmt.setLong(1, uaId);
            stmt.setLong(2, targetId);
            stmt.setString(3, ops);
            stmt.registerOutParameter(4, Types.VARCHAR);
            stmt.execute();
            String errorMsg = stmt.getString(4);
            if (errorMsg!= null && errorMsg.length() > 0) {
                throw new DatabaseException(2000, errorMsg);
            }
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public synchronized void updateAssociation(long uaId, long targetId, boolean inherit, HashSet<String> operations) throws DatabaseException {
        String ops = "";
        for(String op : operations){
            ops += op + ",";
        }
        ops = ops.substring(0, ops.length()-1);
        try {
            CallableStatement stmt = conn.prepareCall("{? = call update_opset(?,?,?)}");
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setLong(2, uaId);
            stmt.setLong(3, targetId);
            stmt.setString(4, ops);
            stmt.execute();
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public synchronized void deleteAssociation(long uaId, long targetId) throws DatabaseException {
        boolean result;
        try {
            CallableStatement stmt = conn.prepareCall("{call delete_association(?,?,?)}");
            stmt.setLong(1, uaId);
            stmt.setLong(2, targetId);
            stmt.registerOutParameter(3, Types.VARCHAR);
            result = stmt.execute();
            String errorMsg = stmt.getString(3);
            if (errorMsg!= null && errorMsg.length() > 0) {
                throw new DatabaseException(2000, errorMsg);
            }
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }
}
