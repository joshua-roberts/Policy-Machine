package gov.nist.csd.pm.pap.loader.graph;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.relationships.NGACAssignment;
import gov.nist.csd.pm.common.model.graph.relationships.NGACAssociation;
import gov.nist.csd.pm.pap.db.sql.SQLConnection;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.db.sql.SQLHelper;
import gov.nist.csd.pm.pap.search.SQLSearch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;

import static gov.nist.csd.pm.common.exceptions.Errors.ERR_DB;

public class SQLGraphLoader implements GraphLoader {

    /**
     * Object to hold connection to Neo4j instance.
     */
    protected SQLConnection conn;

    private DatabaseContext dbCtx;

    /**
     * Create a new GraphLoader from Neo4j, using the provided database connection parameters.
     * @param ctx The parameters to connect to the database
     * @throws PMException If a connection cannot be made to the database
     */
    public SQLGraphLoader(DatabaseContext ctx) throws PMException {
        this.conn = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword(), ctx.getSchema());
        this.dbCtx = ctx;
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
    public HashSet<Node> getNodes() throws PMException {
        try (
                Statement stmt = conn.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery("select node_id from node")
        ){
            SQLSearch search = new SQLSearch(dbCtx);
            HashSet<Node> nodes = new HashSet<>();
            while (rs.next()) {
                long id = rs.getInt(1);
                nodes.add(search.getNode(id));
            }
            return nodes;
        }catch(SQLException e){
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public HashSet<NGACAssignment> getAssignments() throws PMException {
        String sql = String.format("SELECT start_node_id,end_node_id FROM assignment " +
                "join node a on start_node_id = a.node_id and a.node_type_id <> %d " +
                "join node b on end_node_id=b.node_id and b.node_type_id <> %d where assignment.depth=1;", SQLHelper.OS_ID, SQLHelper.OS_ID);
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
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    @Override
    public HashSet<NGACAssociation> getAssociations() throws PMException {
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
            throw new PMException(ERR_DB, e.getMessage());
        }
    }
}
