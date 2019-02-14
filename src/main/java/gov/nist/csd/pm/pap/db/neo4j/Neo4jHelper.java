package gov.nist.csd.pm.pap.db.neo4j;

import gov.nist.csd.pm.common.exceptions.Errors;
import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.common.exceptions.Errors.ERR_DB;

/**
 * Neo4j helper methods
 */
public class Neo4jHelper {

    public static final String COMMA_DELIMETER     = ",\\s*";
    public static final Label  NODE_LABEL          = Label.label("NODE");
    public static final Label  PC_LABEL            = Label.label("PC");
    public static final String ID_PROPERTY         = "id";
    public static final String NAME_PROPERTY       = "name";
    public static final String TYPE_PROPERTY       = "type";
    public static final String OPERATIONS_PROPERTY = "operations";

    public enum RelTypes implements RelationshipType
    {
        ASSIGNED_TO,
        ASSOCIATED_WITH
    }

    /**
     * Given a ResultSet, extract a list of nodes. Each element in the ResultSet is a json representation of a Node
     *
     * @param rs The ResultSet containing the nodes from the database.
     * @return The set of nodes from the ResultSet.
     * @throws PMException If there is an error converting the ResultSet into a list of Nodes.
     */
    public static HashSet<NodeContext> getNodesFromResultSet(ResultSet rs) throws PMException {
        try {
            HashSet<NodeContext> nodes = new HashSet<>();
            while (rs.next()) {
                HashMap map = (HashMap) rs.getObject(1);
                NodeContext node = mapToNode(map);
                nodes.add(node);
            }
            return nodes;
        }
        catch (SQLException e) {
            throw new PMException(ERR_DB, e.getMessage());
        }
    }

    /**
     * Given a map of properties representing a Node, return a Node object.  If the given map is null, then return null.
     * @param map The map to convert into a NodeContext
     * @return A NodeContext representation of the provided map, or null if the map provided was null.
     */
    public static NodeContext mapToNode(Map map) throws PMException {
        if(map == null) {
            return null;
        }

        // first, convert the json to a map
        long id = (long) map.get("id");
        if(id == 0) {
            throw new PMException(Errors.ERR_NO_ID, "encountered an ID of 0 when converting a map to a node");
        }

        String name = (String)map.get("name");
        if(name == null || name.isEmpty()) {
            throw new PMException(Errors.ERR_NULL_NAME, String.format("the node with the ID %d has a null or empty name", id));
        }

        NodeType type = NodeType.toNodeType((String) map.get("type"));
        if(type == null) {
            throw new PMException(Errors.ERR_NULL_TYPE, String.format("the node with the ID %d has a null type", id));
        }

        HashMap<String, String> properties = new HashMap<>();
        for (Object o : map.keySet()) {
            String key = (String)o;
            if(!(key.equals("id") || key.equals("name") || key.equals("type") || key.startsWith("_"))) {
                properties.put(key, (String) map.get(o));
            }
        }

        return new NodeContext(id, name, type, properties);
    }

    /**
     * Convert a Collection of Strings to a cypher array string.
     * (i.e. ["read", "write"] to "['read', 'write']"
     * @param c The HashSet to convert to a string
     * @return A string representation of the given HashSet
     */
    public static String setToCypherArray(HashSet<String> c) {
        String str = "[";
        for (String op : c) {
            op = "'" + op + "'";
            if (str.length()==1) {
                str += op;
            }
            else {
                str += "," + op;
            }
        }
        str += "]";
        return str;
    }

    /**
     * Convert a json string representing a set of strings to an actual set of Strings.
     * @param json The json string to convert into a set of strings
     * @return The set of string converted from the given json string.
     */
    public static HashSet<String> getStringSetFromJson(String json) {
        HashSet<String> set = new HashSet<>();
        if(json != null) {
            String[] opsArr = json.replaceAll("[\\[\\]\"]", "").split(COMMA_DELIMETER);
            if (!opsArr[0].isEmpty()) {
                set.addAll(Arrays.asList(opsArr));
            }
        }
        return set;
    }
}
