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
        this.properties = new HashMap<>();
        this.id = hashID(name, type, properties);
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
        if(properties == null) {
            this.properties = new HashMap<>();
        } else {
            this.properties = properties;
        }
        this.id = hashID(name, type, properties);
    }

    public Node(long id, String name, NodeType type) {
        if(name == null){
            throw new IllegalArgumentException("The name of a node cannot be null");
        }
        if(type == null){
            throw new IllegalArgumentException("The type of a node cannot be null");
        }

        this.id = id;
        this.name = name;
        this.type = type;
        this.properties = new HashMap<>();
    }

    public Node(long id, String name, NodeType type, HashMap<String, String> properties) {
        if(name == null){
            throw new IllegalArgumentException("The name of a node cannot be null");
        }
        if(type == null){
            throw new IllegalArgumentException("The type of a node cannot be null");
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

    public static long hashID(String name, NodeType type, HashMap<String, String> properties) {
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

    public HashMap<String, String> getProperties() {
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

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
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

    public static void main(String[] args) {
        HashMap<String, String> props = new HashMap<>();
        props.put("prop3", "value3");
        props.put("prop2", "value2");
        props.put("prop1", "value1");

        String name = "name123";
        String type = "UA";


        long hash = 0;
        for (String key : props.keySet()) {
            // byte[] bytes = Bytes.concat(Bytes.key.hashCode(), props.get(key).hashCode());
            hash ^= Long.hashCode(key.hashCode()) ^ Long.hashCode(props.get(key).hashCode());
        }

        long id = Long.hashCode(name.hashCode()) ^ Long.hashCode(type.hashCode()) ^ Long.hashCode(hash);
        long id2 = name.hashCode() ^ type.hashCode() ^ hash;
        System.out.println(id);
        System.out.println(id2);
    }
}