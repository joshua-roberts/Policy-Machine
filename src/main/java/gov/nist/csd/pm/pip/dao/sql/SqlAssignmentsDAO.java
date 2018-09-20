package gov.nist.csd.pm.pip.dao.sql;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.pip.dao.AssignmentsDAO;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.*;

public class SqlAssignmentsDAO implements AssignmentsDAO {

    private MySQLConnection mysql;

    public SqlAssignmentsDAO(DatabaseContext ctx) throws DatabaseException {
        mysql = new MySQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
    }

    @Override
    public void createAssignment(Node child, Node parent) throws DatabaseException {
        boolean result;
        try {
            CallableStatement stmt = mysql.getConnection().prepareCall("{call create_assignment(?,?,?)}");
            stmt.setInt(1, (int) parent.getID());
            stmt.setInt(2, (int) child.getID());
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

    @Override
    public synchronized void deleteAssignment(long childId, long parentId) throws DatabaseException {
        boolean result;
        try {
            CallableStatement stmt = mysql.getConnection().prepareCall("{call delete_assignment(?,?,?)}");

            stmt.setLong(1, parentId);
            stmt.setLong(2, childId);
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
