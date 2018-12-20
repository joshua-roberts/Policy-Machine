package gov.nist.csd.pm.pap.prohibitions;

import gov.nist.csd.pm.common.exceptions.Errors;
import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.prohibitions.*;
import gov.nist.csd.pm.pap.db.sql.SQLConnection;
import gov.nist.csd.pm.pap.db.DatabaseContext;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;

import static gov.nist.csd.pm.common.exceptions.Errors.ERR_DB;
import static gov.nist.csd.pm.pap.db.sql.SQLHelper.arrayToString;
import static gov.nist.csd.pm.pap.db.sql.SQLHelper.setToString;

public class SQLProhibitionsDAO implements ProhibitionsDAO {

    private SQLConnection conn;

    public SQLProhibitionsDAO(DatabaseContext ctx) throws PMException {
        conn = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword(), ctx.getSchema());
    }

    @Override
    public synchronized void createProhibition(Prohibition prohibition) throws PMException {
        String name = prohibition.getName();
        ProhibitionSubject subject = prohibition.getSubject();
        HashSet<String> operations = prohibition.getOperations();
        List<ProhibitionNode> nodes = prohibition.getNodes();
        boolean intersection = prohibition.isIntersection();

        String[] resourceCompements = new String[nodes.size()];
        String resourceCompementsStr = "";
        String operationsStr = "";
        int i=0;

        for (ProhibitionNode dr : nodes) {
            resourceCompements[i++] = String.valueOf(dr.getID()) + "-" + String.valueOf(dr.isComplement());
        }

        resourceCompementsStr = arrayToString(resourceCompements, ",");
        operationsStr = setToString(operations, ",");
        System.out.println(operationsStr);
        System.out.println(resourceCompementsStr);
        try{
            CallableStatement stmt = conn.getConnection().prepareCall("{call create_deny(?,?,?,?,?,?,?,?)}");
            stmt.setString(1, name);
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
            stmt.execute();
            String errorMsg = stmt.getString(8);
            if (errorMsg!= null && errorMsg.length() > 0) {
                throw new PMException(ERR_DB, errorMsg);
            }
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public List<Prohibition> getProhibitions() throws PMException {
        return null;
    }

    @Override
    public Prohibition getProhibition(String prohibitionName) throws PMException {
        return null;
    }

    @Override
    public synchronized void deleteProhibition(String prohibitionName) throws PMException {
        try {
            String sql = "DELETE FROM deny WHERE UPPER(deny_name) = UPPER('" + prohibitionName + "')";
            Statement stmt = conn.getConnection().createStatement();
            int affectedRows = stmt.executeUpdate(sql);
            if (affectedRows <=0 ) {
                throw new PMException(ERR_DB, "Error deleting prohibition " + prohibitionName);
            }
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public void updateProhibition(Prohibition prohibition) throws PMException {
        deleteProhibition(prohibition.getName());
        createProhibition(prohibition);
    }

    public synchronized void addResourceToProhibition(String prohibitionName, long resourceId, boolean complement) throws PMException {
        try {
            String sql = "INSERT INTO deny_obj_attribute VALUES ((SELECT deny_id FROM deny WHERE UPPER(deny_name) = UPPER('" + prohibitionName + "')), " + resourceId + ", " + (complement ? 1 : 0) + ");";
            Statement stmt = conn.getConnection().createStatement();
            stmt.execute(sql);
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    public synchronized void deleteProhibitionResource(String prohibitionName, long resourceId) throws PMException {
        try {
            String sql = "DELETE FROM deny_obj_attribute WHERE deny_id = (SELECT deny_id FROM deny WHERE UPPER(deny_name) = UPPER('" + prohibitionName + "')) AND object_attribute_id = " + resourceId;
            System.out.println(sql);
            Statement stmt = conn.getConnection().createStatement();
            int rowsAffected = stmt.executeUpdate(sql);
            if (rowsAffected == 0) {
                throw new PMException(ERR_DB, "Error deleting resource for prohibition " + prohibitionName);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    public void setProhibitionIntersection(String prohibitionName, boolean intersection) throws PMException {
        try{
            String sql = "update deny set deny.is_intersection=" + intersection;
            Statement stmt = conn.getConnection().createStatement();
            stmt.execute(sql);
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    public synchronized void setProhibitionSubject(String prohibitionName, long subjectId, ProhibitionSubjectType subjectType) throws PMException {
        try {
            String sql = "UPDATE deny SET user_attribute_id = " + subjectId + " WHERE deny_name = '" + prohibitionName + "' AND " + "deny_type_id = (SELECT deny_type_id FROM deny_type WHERE abbreviation = '" + subjectType.toString() + "')";
            System.out.println(sql);
            Statement stmt = conn.getConnection().createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    public synchronized void setProhibitionOperations(String prohibitionName, HashSet<String> ops) throws PMException {
        try{
            //first, delete all entries
            String sql = "DELETE FROM deny_operation WHERE deny_id = (SELECT deny_id FROM deny WHERE UPPER(deny_name) = UPPER('" + prohibitionName + "'))";
            Statement stmt = conn.getConnection().createStatement();
            stmt.execute(sql);

            //set the operations
            String oSql = "";
            for (String o : ops) {
                oSql += "((SELECT deny_id FROM deny WHERE UPPER(deny_name) = UPPER('" + prohibitionName + "')), " + "(SELECT operation_id FROM operation WHERE UPPER(name) = UPPER('" + o + "'))), ";
            }
            oSql = oSql.substring(0, oSql.length() - 2);

            sql = "INSERT INTO deny_operation VALUES " + oSql + ";";
            stmt = conn.getConnection().createStatement();
            stmt.execute(sql);
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
    }
}
