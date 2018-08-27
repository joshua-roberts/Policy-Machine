package gov.nist.csd.pm.model.graph;

import gov.nist.csd.pm.model.exceptions.PropertyNotFoundException;

import java.io.Serializable;
import java.util.HashMap;

public class Node implements Serializable{
    private long                    id;
    private String                  name;
    private NodeType                type;
    private HashMap<String, String> properties;
    private String                  content;

    public Node(){
        this.properties = new HashMap<>();
    }

    public Node(String name, NodeType type){
        if(name == null){
            throw new IllegalArgumentException("The name of a node cannot be null");
        }
        if(type == null){
            throw new IllegalArgumentException("The type of a node annot be null");
        }

        this.name = name;
        this.type = type;
        properties = new HashMap<>();
        this.id = hash(name, type, properties);
    }

    public Node(String name, NodeType type, HashMap<String, String> properties){
        if(name == null){
            throw new IllegalArgumentException("The name of a node cannot be null");
        }
        if(type == null){
            throw new IllegalArgumentException("The type of a node cannot be null");
        }

        this.name = name;
        this.type = type;
        this.properties = properties;
        this.id = hash(name, type, properties);
    }

    private static long hash(String name, NodeType type, HashMap<String, String> properties) {
        return name.hashCode() * type.hashCode() * properties.hashCode();
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

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public boolean hasProperty(String key, String value){
        String foundValue = this.properties.get(key);
        return foundValue != null && foundValue.equals(value);
    }

    public boolean hasProperty(String key){
        return this.properties.get(key) != null;
    }

    public String getProperty(String key) throws PropertyNotFoundException {
        String foundValue = this.properties.get(key);
        if (foundValue == null) {
            throw new PropertyNotFoundException(this.id, key);
        }

        return foundValue;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean equals(Object o){
        if(o instanceof Node){
            Node n = (Node) o;
            return this.id == n.id;
        }
        return false;
    }

    public String toString() {
        return name + ":" + type + ":" + id + ":" + properties;
    }
}