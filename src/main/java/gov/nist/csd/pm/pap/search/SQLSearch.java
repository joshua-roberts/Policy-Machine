package gov.nist.csd.pm.pap.search;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pap.db.sql.SQLConnection;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.db.sql.SQLHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static gov.nist.csd.pm.common.exceptions.Errors.ERR_DB;

/**
 * Implementation of the Search interface using SQL
 */
public class SQLSearch implements Search {

    /**
     * Object to hold connection to SQL instance.
     */
    protected SQLConnection conn;

    /**
     * Create a new Search with the given SQL connection context.
     */
    public SQLSearch(DatabaseContext ctx) throws PMException {
        this.conn = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword(), ctx.getSchema());
    }

    @Override
    public HashSet<Node> search(String name, String type, Map<String, String> properties) throws PMException {
        return null;
    }

    @Override
    public Node getNode(long id) throws PMException {
        try (
                Statement stmt = conn.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery("select node_id,name,node_type_id from node where node_id="+id);
        ) {
            rs.next();
            String name = rs.getString(2);
            NodeType type = SQLHelper.toNodeType(rs.getInt(3));
            HashMap<String, String> properties = getNodeProps(id);

            return new Node(id, name, type, properties);
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    private HashMap<String, String> getNodeProps(long nodeID) throws PMException {
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
            throw new PMException(ERR_DB, e.getMessage());
        }
    }
}
