package gov.nist.csd.pm.pip.dao.neo4j;

import com.google.gson.*;
import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidPropertyException;
import gov.nist.csd.pm.model.graph.Association;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.prohibitions.Prohibition;
import gov.nist.csd.pm.model.prohibitions.ProhibitionResource;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubject;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.*;

import static gov.nist.csd.pm.model.Constants.COMMA_DELIMETER;
import static gov.nist.csd.pm.pep.response.ApiResponseCodes.ERR_NEO;


public class Neo4jConnection {

    private Connection connection;

    public Neo4jConnection(String host, int port, String username, String password) throws DatabaseException {
        try {
            Driver driver = new org.neo4j.jdbc.Driver();
            DriverManager.registerDriver(driver);
            connection = DriverManager.getConnection("jdbc:neo4j:http://" + host + ":" + port + "", username, password);
        } catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    public ResultSet execute(String cypher) throws DatabaseException {
        try {
            PreparedStatement stmt = connection.prepareStatement(cypher);
            return stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    public List<Node> getNodesFromResultSet(ResultSet rs) throws DatabaseException {
        List<Node> nodes = new ArrayList<>();

        try {
            while (rs.next()) {
                Node node = getNodeFromJson(rs.getString(1));
                nodes.add(node);
            }
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }

        return nodes;
    }

    protected String setToCypherArray(HashSet<String> set) {
        String str = "[";
        for (String op : set) {
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

    public Node getNodeFromJson(String json){
        return new Gson().fromJson(json, Node.class);
    }

    public HashSet<String> getStringSetFromJson(String json) {
        HashSet<String> ops = new HashSet<>();
        String[] opsArr = json.replaceAll("[\\[\\]\"]", "").split(COMMA_DELIMETER);
        if(!opsArr[0].isEmpty()){
            ops.addAll(Arrays.asList(opsArr));
        }
        return ops;
    }

    public List<Long> toLongList(String json){
        List<Long> ids = new ArrayList<>();
        String[] idArr = json.replaceAll("[\\[\\]]", "").split(COMMA_DELIMETER);
        if(!idArr[0].isEmpty()){
            for(String id : idArr) {
                ids.add(Long.valueOf(id));
            }
        }
        return ids;
    }

    public List<Integer> toIntList(String json){
        List<Integer> ids = new ArrayList<>();
        String[] idArr = json.replaceAll("[\\[\\]]", "").split(COMMA_DELIMETER);
        if(!idArr[0].isEmpty()){
            for(String id : idArr) {
                ids.add(Integer.valueOf(id));
            }
        }
        return ids;
    }

    public Map<String, String> strToPropertyMap(String propArr) {
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

    public Association getAssociationFromJson(String json) {
        return new Gson().fromJson(json, Association.class);
    }

    public String toJson(Object o, boolean pretty){
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

    public Map<String, String> getPropertiesFromJson(String json) throws InvalidPropertyException {
        Map<String, String> props = new HashMap<>();
        JsonElement je = new JsonParser().parse(json);
        JsonObject jo = je.getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entries = jo.entrySet();

        for(Map.Entry<String, JsonElement> prop : entries){
            if(prop.getKey().equals("name") || prop.getKey().equals("type") || prop.getKey().equals("id")){
                continue;
            }

            props.put(prop.getKey(), StringUtils.strip(prop.getValue().toString(), "\""));
        }
        return props;
    }

    public ProhibitionSubject getProhibitionSubject(String json) {
        return new Gson().fromJson(json, ProhibitionSubject.class);
    }

    public Prohibition getProhibition(String json) {
        return new Gson().fromJson(json, Prohibition.class);
    }

    public List<ProhibitionResource> getProhibitionResources(String json) {
        json = json.replaceAll("[\\[\\]]", "").replaceAll("\\},\\{", "}|{");
        String[] jsonArr = json.split("\\|");
        List<ProhibitionResource> drs = new ArrayList<>();
        for(String j : jsonArr){
            drs.add(new Gson().fromJson(j, ProhibitionResource.class));
        }
        return drs;
    }
}
