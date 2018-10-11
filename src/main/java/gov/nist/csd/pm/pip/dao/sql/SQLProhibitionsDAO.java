package gov.nist.csd.pm.pip.dao.sql;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.pip.dao.ProhibitionsDAO;
import gov.nist.csd.pm.model.prohibitions.ProhibitionResource;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubject;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubjectType;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.*;
import java.util.HashSet;

import static gov.nist.csd.pm.pip.dao.sql.SQLConnection.arrayToString;
import static gov.nist.csd.pm.pip.dao.sql.SQLConnection.setToString;

public class SQLProhibitionsDAO implements ProhibitionsDAO {
    private SQLConnection mysql;

    public SQLProhibitionsDAO(DatabaseContext ctx) throws DatabaseException {
        mysql = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword());
    }

    @Override
    public synchronized void createProhibition(String prohibitionName, HashSet<String> operations, boolean intersection, ProhibitionResource[] resources, ProhibitionSubject subject) throws DatabaseException {
        String[] resourceCompements = new String[resources.length];
        String resourceCompementsStr = "";
        String operationsStr = "";
        int i=0;
        boolean result;

        for (ProhibitionResource dr : resources) {
            resourceCompements[i++] = String.valueOf(dr.getResourceID()) + "-" + String.valueOf(dr.isComplement());
        }

        resourceCompementsStr = arrayToString(resourceCompements, ",");
        operationsStr = setToString(operations, ",");
        System.out.println(operationsStr);
        System.out.println(resourceCompementsStr);
        try{
            CallableStatement stmt = mysql.getConnection().prepareCall("{call create_deny(?,?,?,?,?,?,?,?)}");
            stmt.setString(1, prohibitionName);
            stmt.setString(2, subject.getSubjectType().toString());
            stmt.setString(3, operationsStr);
            stmt.setBoolean(4, intersection);
            stmt.setString(5, resourceCompementsStr);
            if (subject.getSubjectType().toString().equalsIgnoreCase("u") || subject.getSubjectType().toString().equalsIgnoreCase("ua")) {
                stmt.setString(6, String.valueOf(subject.getSubjectID()));
                stmt.setString(7, null);
            } else {
                stmt.setString(6, null);
                stmt.setString(7, String.valueOf(subject.getSubjectID()));
            }
            stmt.registerOutParameter(8, Types.VARCHAR);
            result = stmt.execute();
            String errorMsg = stmt.getString(8);
            if (errorMsg!= null && errorMsg.length() > 0) {
                throw new DatabaseException(2000, errorMsg);
            }

        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    //TODO: Need to test
    @Override
    public synchronized void deleteProhibition(String prohibitionName) throws DatabaseException {
        try {
            String sql = "DELETE FROM deny WHERE UPPER(deny_name) = UPPER('" + prohibitionName + "')";
            Statement stmt = mysql.getConnection().createStatement();
            int affectedRows = stmt.executeUpdate(sql);
            if (affectedRows <=0 ) {
                throw new DatabaseException(8000, "Error deleting prohibition " + prohibitionName);
            }
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public synchronized void addResourceToProhibition(String prohibitionName, long resourceID, boolean complement) throws DatabaseException {
        try {
            String sql = "INSERT INTO deny_obj_attribute VALUES ((SELECT deny_id FROM deny WHERE UPPER(deny_name) = UPPER('" + prohibitionName + "')), " + resourceID + ", " + (complement ? 1 : 0) + ");";
            Statement stmt = mysql.getConnection().createStatement();
            stmt.execute(sql);
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public synchronized void deleteProhibitionResource(String prohibitionName, long resourceID) throws DatabaseException {
        try {
            String sql = "DELETE FROM deny_obj_attribute WHERE deny_id = (SELECT deny_id FROM deny WHERE UPPER(deny_name) = UPPER('" + prohibitionName + "')) AND object_attribute_id = " + resourceID;
            System.out.println(sql);
            Statement stmt = mysql.getConnection().createStatement();
            int rowsAffected = stmt.executeUpdate(sql);
            if (rowsAffected == 0) {
                throw new DatabaseException(8000, "Error deleting resource for prohibition " + prohibitionName);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public void setProhibitionIntersection(String prohibitionName, boolean intersection) throws DatabaseException {
        try{
            String sql = "update deny set deny.is_intersection=" + intersection;
            Statement stmt = mysql.getConnection().createStatement();
            stmt.execute(sql);
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public synchronized void setProhibitionSubject(String prohibitionName, long subjectID, ProhibitionSubjectType subjectType) throws DatabaseException {
        try {
            String sql = "UPDATE deny SET user_attribute_id = " + subjectID + " WHERE deny_name = '" + prohibitionName + "' AND " + "deny_type_id = (SELECT deny_type_id FROM deny_type WHERE abbreviation = '" + subjectType.toString() + "')";
            System.out.println(sql);
            Statement stmt = mysql.getConnection().createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public synchronized void setProhibitionOperations(String prohibitionName, HashSet<String> ops) throws DatabaseException {
        try{
            //first, delete all entries
            String sql = "DELETE FROM deny_operation WHERE deny_id = (SELECT deny_id FROM deny WHERE UPPER(deny_name) = UPPER('" + prohibitionName + "'))";
            Statement stmt = mysql.getConnection().createStatement();
            stmt.execute(sql);

            //set the operations
            String oSql = "";
            for (String o : ops) {
                oSql += "((SELECT deny_id FROM deny WHERE UPPER(deny_name) = UPPER('" + prohibitionName + "')), " + "(SELECT operation_id FROM operation WHERE UPPER(name) = UPPER('" + o + "'))), ";
            }
            oSql = oSql.substring(0, oSql.length() - 2);

            sql = "INSERT INTO deny_operation VALUES " + oSql + ";";
            stmt = mysql.getConnection().createStatement();
            stmt.execute(sql);
        }
        catch (SQLException e) {
            throw new DatabaseException(e.getErrorCode(), e.getMessage());
        }
    }
}
