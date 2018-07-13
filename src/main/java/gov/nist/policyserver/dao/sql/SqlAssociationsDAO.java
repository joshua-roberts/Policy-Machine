package gov.nist.policyserver.dao.sql;

import gov.nist.policyserver.dao.AssociationsDAO;
import gov.nist.policyserver.exceptions.DatabaseException;

import java.sql.*;
import java.util.HashSet;

public class SqlAssociationsDAO implements AssociationsDAO {

    private Connection conn;

    public SqlAssociationsDAO(Connection connection) {
        this.conn = connection;
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
