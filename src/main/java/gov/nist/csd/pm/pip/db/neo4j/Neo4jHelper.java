package gov.nist.csd.pm.pip.db.neo4j;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidNodeTypeException;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.model.graph.relationships.NGACAssociation;
import gov.nist.csd.pm.model.prohibitions.Prohibition;
import gov.nist.csd.pm.model.prohibitions.ProhibitionResource;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.model.Constants.COMMA_DELIMETER;
import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_DB;

/**
 * Neo4j helper methods
 */
public class Neo4jHelper {

    /**
     * Given a ResultSet, extract a list of nodes. Each element in the ResultSet is a json representation of a Node
     */
    public static HashSet<Node> getNodesFromResultSet(ResultSet rs) throws DatabaseException {
        try {
            HashSet<Node> nodes = new HashSet<>();
            while (rs.next()) {
                Node node = getNodeFromJson(rs.getString(1));
                nodes.add(node);
            }
            return nodes;
        }
        catch (SQLException | InvalidNodeTypeException e) {
            throw new DatabaseException(ERR_DB, e.getMessage());
        }
    }

    /**
     * Given a json representation of an Node, return an Node object.
     */
    public static Node getNodeFromJson(String json) throws InvalidNodeTypeException {
        // first, convert the json to a map
        LinkedTreeMap map = new Gson().fromJson(json, LinkedTreeMap.class);
        long id = (long) (double)map.get("id");
        String name = (String)map.get("name");
        NodeType type = NodeType.toNodeType((String) map.get("type"));
        HashMap<String, String> properties = new HashMap<>();
        for (Object o : map.keySet()) {
            properties.put((String)o, (String)map.get(o));
        }

        return new Node()
                .id(id)
                .name(name)
                .type(type)
                .properties(properties);
    }

    /**
     * Convert a Colection of Strings to a cypher array string.
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
        String[] opsArr = json.replaceAll("[\\[\\]\"]", "").split(COMMA_DELIMETER);
        if(!opsArr[0].isEmpty()){
            set.addAll(Arrays.asList(opsArr));
        }
        return set;
    }

    /**
     * Convert a json string representing a set of numbers into a List of Integers.
     */
    public static List<Integer> toIntList(String json){
        List<Integer> ids = new ArrayList<>();
        String[] idArr = json.replaceAll("[\\[\\]]", "").split(COMMA_DELIMETER);
        if(!idArr[0].isEmpty()){
            for(String id : idArr) {
                ids.add(Integer.valueOf(id));
            }
        }
        return ids;
    }

    /**
     * Convert a string representing a Map into an instance of a Map.
     */
    public static Map<String, String> strToPropertyMap(String propArr) {
        Map<String, String> properties = new HashMap<>();

        String[] propsStrArr = propArr.replaceAll("[\\[\\]\"]", "").split(COMMA_DELIMETER);
        if(!propsStrArr[0].isEmpty()){
            for(String prop : propsStrArr) {
                String[] split = prop.split("=");
                properties.put(split[0], split[1]);
            }
        }

        return properties;
    }

    /**
     * Convert a json string representing an Association in to an Association instance.
     * @param json
     * @return
     */
    public static NGACAssociation getAssociationFromJson(String json) {
        return new Gson().fromJson(json, NGACAssociation.class);
    }

    /**
     * Return the json representation of the given object.
     * @param o The object to convert to json.
     * @param pretty Whether or not the returned json is formatted or just a raw string
     * @return
     */
    public static String toJson(Object o, boolean pretty){
        String s = new Gson().toJson(o);
        Gson gson;
        if(pretty) {
            gson = new GsonBuilder().setPrettyPrinting().create();
        }else{
            gson = new GsonBuilder().create();
        }
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(s);
        return gson.toJson(je);
    }

    /**
     * Given a json string, convert it to an instance of a ProhibitionSubject
     */
    static ProhibitionSubject getProhibitionSubject(String json) {
        return new Gson().fromJson(json, ProhibitionSubject.class);
    }

    /**
     * Convert a json string to a Prohibition
     */
    static Prohibition getProhibition(String json) {
        return new Gson().fromJson(json, Prohibition.class);
    }

    /**
     * Extract a list of ProhibitionResources from a json string.
     */
    public static List<ProhibitionResource> getProhibitionResources(String json) {
        json = json.replaceAll("[\\[\\]]", "").replaceAll("\\},\\{", "}|{");
        String[] jsonArr = json.split("\\|");
        List<ProhibitionResource> drs = new ArrayList<>();
        for(String j : jsonArr){
            drs.add(new Gson().fromJson(j, ProhibitionResource.class));
        }
        return drs;
    }
}
