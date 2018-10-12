package gov.nist.csd.pm.pip.dao.sql;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.pip.dao.AssociationsDAO;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.*;
import java.util.HashSet;

public class SQLAssociationsDAO implements AssociationsDAO {

    private SQLConnection mysql;

    public SQLAssociationsDAO(DatabaseContext ctx) throws DatabaseException {
        mysql = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword(), ctx.getSchema());
    }
    @Override
    public synchronized void createAssociation(long uaId, long targetId, HashSet<String> operations) throws DatabaseException {
        String ops = "";
        for(String op : operations){
            ops += op + ",";
        }
        ops = ops.substring(0, ops.length()-1);
        try (
            CallableStatement stmt = mysql.getConnection().prepareCall("{call create_association(?,?,?,?)}");
        ) {
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
    public synchronized void updateAssociation(long uaId, long targetId, HashSet<String> operations) throws DatabaseException {
        String ops = "";
        for(String op : operations){
            ops += op + ",";
        }
        ops = ops.substring(0, ops.length()-1);
        try {
            CallableStatement stmt = mysql.getConnection().prepareCall("{? = call update_opset(?,?,?)}");
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
            CallableStatement stmt = mysql.getConnection().prepareCall("{call delete_association(?,?,?)}");
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
