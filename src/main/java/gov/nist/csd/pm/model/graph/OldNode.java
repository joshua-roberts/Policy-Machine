package gov.nist.csd.pm.model.graph;

import gov.nist.csd.pm.model.exceptions.PropertyNotFoundException;
import gov.nist.csd.pm.model.graph.nodes.NodeType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class OldNode implements Serializable{
    private long                id;
    private String              name;
    private NodeType            type;
    private Map<String, String> properties;
    private String              content;
    
    private static final String NULL_NAME_ERR = "The name of a node cannot be null";
    private static final String NULL_TYPE_ERR = "The type of a node cannot be null";

    public OldNode(){
        this.properties = new HashMap<>();
    }

    public OldNode(String name, NodeType type){
        if(name == null){
            throw new IllegalArgumentException(NULL_NAME_ERR);
        }
        if(type == null){
            throw new IllegalArgumentException(NULL_TYPE_ERR);
        }

        this.name = name;
        this.type = type;
        this.properties = new HashMap<>();
        this.id = hashID(name, type, properties);
    }

    public OldNode(String name, NodeType type, Map<String, String> properties){
        if(name == null){
            throw new IllegalArgumentException(NULL_NAME_ERR);
        }
        if(type == null){
            throw new IllegalArgumentException(NULL_TYPE_ERR);
        }

        this.name = name;
        this.type = type;
        if(properties == null) {
            this.properties = new HashMap<>();
        } else {
            this.properties = properties;
        }
        this.id = hashID(name, type, properties);
    }

    public OldNode(long id, String name, NodeType type) {
        if(name == null){
            throw new IllegalArgumentException(NULL_NAME_ERR);
        }
        if(type == null){
            throw new IllegalArgumentException(NULL_TYPE_ERR);
        }

        this.id = id;
        this.name = name;
        this.type = type;
        this.properties = new HashMap<>();
    }

    public OldNode(long id, String name, NodeType type, Map<String, String> properties) {
        if(name == null){
            throw new IllegalArgumentException(NULL_NAME_ERR);
        }
        if(type == null){
            throw new IllegalArgumentException(NULL_TYPE_ERR);
        }

        this.id = id;
        this.name = name;
        this.type = type;
        if(properties == null) {
            this.properties = new HashMap<>();
        } else {
            this.properties = properties;
        }
    }

    public static long hashID(String name, NodeType type, Map<String, String> properties) {
        long propsHash = 0;
        for (String key : properties.keySet()) {
            propsHash ^= key.hashCode() ^ properties.get(key).hashCode();
        }
        return name.hashCode() ^ type.hashCode() ^ propsHash;
    }

    public long getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public NodeType getType() {
        return type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public boolean hasProperty(String key, String value){
        String foundValue = this.properties.get(key);
        return foundValue != null && foundValue.equals(value);
    }

    public boolean hasPropertyKey(String key){
        return this.properties.get(key) != null;
    }

    public String getProperty(String key) throws PropertyNotFoundException {
        String foundValue = this.properties.get(key);
        if (foundValue == null) {
            throw new PropertyNotFoundException(this.id, key);
        }

        return foundValue;
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean equals(Object o){
        if(o instanceof OldNode){
            OldNode n = (OldNode) o;
            return this.id == n.id;
        }
        return false;
    }

    public String toString() {
        return name + ":" + type + ":" + id + ":" + properties;
    }
}