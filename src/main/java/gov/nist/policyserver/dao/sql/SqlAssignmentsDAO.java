package gov.nist.policyserver.dao.sql;

import gov.nist.policyserver.dao.AssignmentsDAO;
import gov.nist.policyserver.dao.AssociationsDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.InvalidNodeTypeException;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.relationships.Assignment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlAssignmentsDAO implements AssignmentsDAO {


    private Connection conn;

    public SqlAssignmentsDAO(Connection connection) {
        this.conn = connection;
    }

    @Override
    public synchronized void createAssignment(long childId, long parentId) throws DatabaseException {
        boolean result;
        try {
            CallableStatement stmt = conn.prepareCall("{call create_assignment(?,?,?)}");
            stmt.setInt(1, (int) parentId);
            stmt.setInt(2, (int) childId);
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
            CallableStatement stmt = conn.prepareCall("{call delete_assignment(?,?,?)}");

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
