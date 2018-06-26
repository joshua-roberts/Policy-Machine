package gov.nist.policyserver.helpers;

import com.google.gson.*;
import gov.nist.policyserver.exceptions.InvalidPropertyException;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.Property;
import gov.nist.policyserver.model.graph.relationships.Association;
import gov.nist.policyserver.model.prohibitions.Prohibition;
import gov.nist.policyserver.model.prohibitions.ProhibitionResource;
import gov.nist.policyserver.model.prohibitions.ProhibitionSubject;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static gov.nist.policyserver.common.Constants.COMMA_DELIMETER;

public class JsonHelper {

    public static Node getNodeFromJson(String json){
        return new Gson().fromJson(json, Node.class);
    }

    public static HashSet<String> getStringSetFromJson(String json) {
        HashSet<String> ops = new HashSet<>();
        String[] opsArr = json.replaceAll("[\\[\\]\"]", "").split(COMMA_DELIMETER);
        if(!opsArr[0].isEmpty()){
            ops.addAll(Arrays.asList(opsArr));
        }
        return ops;
    }

    public static List<Long> toLongList(String json){
        List<Long> ids = new ArrayList<>();
        String[] idArr = json.replaceAll("[\\[\\]]", "").split(COMMA_DELIMETER);
        if(!idArr[0].isEmpty()){
            for(String id : idArr) {
                ids.add(Long.valueOf(id));
            }
        }
        return ids;
    }

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

    public static List<Property> strToPropertyList(String propArr) throws InvalidPropertyException {
        List<Property> properties = new ArrayList<>();

        String[] propsStrArr = propArr.replaceAll("[\\[\\]\"]", "").split(COMMA_DELIMETER);
        if(!propsStrArr[0].isEmpty()){
            for(String prop : propsStrArr) {
                String[] split = prop.split("=");
                Property property = new Property(split[0], split[1]);
                properties.add(property);
            }
        }

        return properties;
    }

    public static Association getAssociationFromJson(String json) {
        return new Gson().fromJson(json, Association.class);
    }

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

    public static List<Property> getPropertiesFromJson(String json) {
        List<Property> props = new ArrayList<>();
        JsonElement je = new JsonParser().parse(json);
        JsonObject jo = je.getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entries = jo.entrySet();

        for(Map.Entry<String, JsonElement> prop : entries){
            if(prop.getKey().equals("name") || prop.getKey().equals("type") || prop.getKey().equals("id")){
                continue;
            }
            try {
                Property property = new Property(prop.getKey(), StringUtils.strip(prop.getValue().toString(), "\""));
                props.add(property);
            }
            catch (InvalidPropertyException e) {
                e.printStackTrace();
            }
        }
        return props;
    }

    public static ProhibitionSubject getProhibitionSubject(String json) {
        return new Gson().fromJson(json, ProhibitionSubject.class);
    }

    public static Prohibition getProhibition(String json) {
        return new Gson().fromJson(json, Prohibition.class);
    }

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
