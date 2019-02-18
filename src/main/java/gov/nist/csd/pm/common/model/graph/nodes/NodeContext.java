package gov.nist.csd.pm.common.model.graph.nodes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

/**
 * Stores information needed for a node.
 */
public class NodeContext implements Serializable {
    private long                    parentID;
    private long                    id;
    private String                  name;
    private NodeType                type;
    private HashMap<String, String> properties;

    public NodeContext() {
        this.properties = new HashMap<>();
    }

    public NodeContext(String name, NodeType type, HashMap<String, String> properties){
        this.name = name;
        this.type = type;
        this.properties = properties == null ? new HashMap<>() : properties;
    }

    public NodeContext(long id, String name, NodeType type, HashMap<String, String> properties) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.properties = properties == null ? new HashMap<>() : properties;
    }

    public NodeContext(long id, NodeType type) {
        this.id = id;
        this.type = type;
    }

    public NodeContext parentID(long parentID) {
        this.parentID = parentID;
        return this;
    }

    public NodeContext id(long id) {
        if (id == 0) {
            throw new IllegalArgumentException("a node cannot have an ID of 0");
        }

        this.id = id;
        return this;
    }

    public NodeContext name(String name) {
        if (name == null || name .isEmpty()) {
            throw new IllegalArgumentException("a node can not have a null or empty name");
        }

        this.name = name;
        return this;
    }

    public NodeContext type(NodeType type) {
        if(type == null) {
            throw new IllegalArgumentException("a type cannot be null");
        }
        this.type = type;
        return this;
    }

    public NodeContext properties(HashMap<String, String> properties) throws IllegalArgumentException {
        if (properties == null) {
            properties = new HashMap<>();
        }

        this.properties = properties;
        return this;
    }

    public NodeContext property(String key, String value) throws IllegalArgumentException {
        if (key == null || value == null) {
            throw new IllegalArgumentException("a node cannot have a property with a null key or value");
        }

        this.properties.put(key, value);
        return this;
    }

    public long getParentID() {
        return parentID;
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

    /**
     * Two nodes are equal if their IDs are the same.
     * @param o The object to check for equality.
     * @return true if the two objects are the same, false otherwise.
     */
    public boolean equals(Object o){
        if(o instanceof NodeContext){
            NodeContext n = (NodeContext) o;
            return this.id == n.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String toString() {
        return name + ":" + type + ":" + id + ":" + properties;
    }
}
