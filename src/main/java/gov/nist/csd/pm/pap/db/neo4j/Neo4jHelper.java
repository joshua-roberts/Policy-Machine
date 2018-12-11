package gov.nist.csd.pm.pap.db.neo4j;

import gov.nist.csd.pm.common.exceptions.DatabaseException;
import gov.nist.csd.pm.common.exceptions.InvalidNodeTypeException;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.common.exceptions.ErrorCodes.ERR_DB;

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
     */
    public static HashSet<Node> getNodesFromResultSet(ResultSet rs) throws DatabaseException {
        try {
            HashSet<Node> nodes = new HashSet<>();
            while (rs.next()) {
                HashMap map = (HashMap) rs.getObject(1);
                Node node = mapToNode(map);
                nodes.add(node);
            }
            return nodes;
        }
        catch (SQLException | InvalidNodeTypeException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    /**
     * Given a map of properties representing a Node, return a Node object.
     */
    public static Node mapToNode(Map map) throws InvalidNodeTypeException {
        // first, convert the json to a map
        long id = (long) map.get("id");
        String name = (String)map.get("name");
        NodeType type = NodeType.toNodeType((String) map.get("type"));
        HashMap<String, String> properties = new HashMap<>();
        for (Object o : map.keySet()) {
            String key = (String)o;
            if(!(key.equals("id") || key.equals("name") || key.equals("type") || key.startsWith("_"))) {
                properties.put(key, (String) map.get(o));
            }
        }

        return new Node(id, name, type, properties);
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
