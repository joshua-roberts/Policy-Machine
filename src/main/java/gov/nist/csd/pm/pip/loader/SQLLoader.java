package gov.nist.csd.pm.pip.loader;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidNodeTypeException;
import gov.nist.csd.pm.model.graph.NGAC;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.model.graph.relationships.NGACAssignment;
import gov.nist.csd.pm.model.graph.relationships.NGACAssociation;
import gov.nist.csd.pm.pip.db.neo4j.Neo4jConnection;
import gov.nist.csd.pm.pip.db.sql.SQLConnection;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_DB;

public class SQLLoader implements Loader {

    /**
     * Object to hold connection to Neo4j instance.
     */
    protected SQLConnection conn;

    /**
     * Create a new Loader from Neo4j, using the provided database connection parameters.
     * @param ctx The parameters to connect to the database
     * @throws DatabaseException If a connection cannot be made to the database
     */
    public SQLLoader(DatabaseContext ctx) throws DatabaseException {
        conn = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword(), ctx.getSchema());
    }

    @Override
    public HashSet<Long> getPolicies() throws LoaderException {
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
            throw new LoaderException(e.getMessage());
        }
    }

    @Override
    public HashSet<Long> getNodes() throws LoaderException {
        try (
                Statement stmt = conn.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery("select node_id from node")
        ){
            HashSet<Long> nodes = new HashSet<>();
            while (rs.next()) {
                long id = rs.getInt(1);
                nodes.add(id);
            }
            return nodes;
        }catch(SQLException e){
            throw new LoaderException(e.getMessage());
        }
    }

    @Override
    public HashSet<NGACAssignment> getAssignments() throws LoaderException {
        String sql = String.format("SELECT start_node_id,end_node_id FROM assignment join node a on start_node_id = a.node_id and a.node_type_id <> %d join node b on end_node_id=b.node_id and b.node_type_id <> %d where assignment.depth=1;", NodeType.OS_ID, NodeType.OS_ID);
        try(
                Statement stmt = conn.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ) {
            HashSet<NGACAssignment> assignments = new HashSet<>();
            while(rs.next()){
                long startID = rs.getLong(1);
                long endID = rs.getLong(2);
                assignments.add(new NGACAssignment(endID, startID));
            }
            return assignments;
        } catch(SQLException e){
            throw new LoaderException(e.getMessage());
        }
    }

    @Override
    public HashSet<NGACAssociation> getAssociations() throws LoaderException {
        try (
                Statement stmt = conn.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery("SELECT ua_id, get_operations(opset_id),target_id FROM association join node a on ua_id = a.node_id join node b on target_id=b.node_id");
        ) {
            HashSet<NGACAssociation> associations = new HashSet<>();
            while (rs.next()) {
                long uaID = rs.getLong(1);
                HashSet<String> ops = new HashSet<>(Arrays.asList(rs.getString(2).split(",")));
                long targetID = rs.getInt(3);

                associations.add(new NGACAssociation(uaID, targetID, ops));
            }
            return associations;
        } catch(SQLException e){
            throw new LoaderException(e.getMessage());
        }
    }
}
