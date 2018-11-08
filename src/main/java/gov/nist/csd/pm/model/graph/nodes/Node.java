package gov.nist.csd.pm.model.graph.nodes;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Node in an NGAC graph
 */
public class Node {
    private long                    id;
    private String                  name;
    private NodeType                type;
    private HashMap<String, String> properties;

    private static final String NULL_NAME_ERR = "The name of a node cannot be null";
    private static final String NULL_TYPE_ERR = "The type of a node cannot be null";

    /**
     * Method to hash the name, type and namespace of a node and return a Long value
     * @param name The name of the node.
     * @param type The type of the node.
     * @param namespace The namespace of the node.
     * @return A Long value representing the hashing of the name, type, and namespace.
     */
    public static long hashID(String name, NodeType type, String namespace) {
        //if the namespace is null, the node is in the "default" namespace.
        //set the namespace to default to improve hashing of ID
        if(namespace == null || namespace.isEmpty()) {
            namespace = "default";
        }

        long result = 17;
        result = 37*result + name.hashCode();
        result = 37*result + type.hashCode();
        result = 37*result + namespace.hashCode();
        return result;
    }


    public Node() {
        this.properties = new HashMap<>();
    }

    public Node(String name, NodeType type){
        if(name == null){
            throw new IllegalArgumentException(NULL_NAME_ERR);
        }
        if(type == null){
            throw new IllegalArgumentException(NULL_TYPE_ERR);
        }

        this.name = name;
        this.type = type;
        this.properties = new HashMap<>();
    }

    public Node(String name, NodeType type, HashMap<String, String> properties){
        if(name == null){
            throw new IllegalArgumentException(NULL_NAME_ERR);
        }
        if(type == null){
            throw new IllegalArgumentException(NULL_TYPE_ERR);
        }

        this.name = name;
        this.type = type;
        this.properties = properties == null ? new HashMap<>() : properties;
    }

    public Node(long id, String name, NodeType type) {
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

    public Node(long id, String name, NodeType type, HashMap<String, String> properties) {
        if(name == null){
            throw new IllegalArgumentException(NULL_NAME_ERR);
        }
        if(type == null){
            throw new IllegalArgumentException(NULL_TYPE_ERR);
        }

        this.id = id;
        this.name = name;
        this.type = type;
        this.properties = properties == null ? new HashMap<>() : properties;
    }

    public Node(long id, NodeType type) {
        if(type == null){
            throw new IllegalArgumentException(NULL_TYPE_ERR);
        }

        this.id = id;
        this.type = type;
        this.properties = properties == null ? new HashMap<>() : properties;
    }

    /**
     * Builder method to add an ID to the node.
     * @param id The ID to add to the node.
     * @return The current node with the given ID.
     * @throws IllegalArgumentException If the ID provided is 0.
     */
    public Node id(long id) throws IllegalArgumentException {
        if (id == 0) {
            throw new IllegalArgumentException("a node cannot have an ID of 0");
        }

        this.id = id;
        return this;
    }

    /**
     * Builder method to add a name to the node.
     * @param name The name to add to the node.
     * @return The current node with the given name.
     * @throws IllegalArgumentException If the name provided is null or empty.
     */
    public Node name(String name) throws IllegalArgumentException {
        if (name == null || name .isEmpty()) {
            throw new IllegalArgumentException("a node can not have a null or empty name");
        }

        this.name = name;
        return this;
    }

    /**
     * Builder method to add properties to the node.
     * @param properties The map of properties to add to the node.
     * @return The current node with the given properties.
     * @throws IllegalArgumentException If the provided properties map is null.
     */
    public Node properties(HashMap<String, String> properties) throws IllegalArgumentException {
        if (properties == null) {
            throw new IllegalArgumentException("a node cannot have null properties");
        }

        this.properties = properties;
        return this;
    }

    /**
     * Add the property specified by the key value pair to the current node.
     * @param key The key of the property to add.
     * @param value The value of the property to add.
     * @return The current node with the given property added.
     * @throws IllegalArgumentException If either the provided key or value is null.
     */
    public Node property(String key, String value) throws IllegalArgumentException {
        if (key == null || value == null) {
            throw new IllegalArgumentException("a node cannot have a property with a null key or value");
        }

        this.properties.put(key, value);
        return this;
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
