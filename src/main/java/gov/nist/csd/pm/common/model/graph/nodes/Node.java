package gov.nist.csd.pm.common.model.graph.nodes;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Objects;

import static gov.nist.csd.pm.common.constants.Properties.NAMESPACE_PROPERTY;

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
        this.id = hashID(name, type, properties.get(NAMESPACE_PROPERTY));
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
        this.id = hashID(name, type, this.properties.get(NAMESPACE_PROPERTY));
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

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String toString() {
        return name + ":" + type + ":" + id + ":" + properties;
    }

    // The following are utility methods for nodes.

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

    /**
     * Utility method to hash a password of a user. This will be used by the session and node service classes.
     * @param password The plaintext password to hash.
     * @return The hash of the password;
     */
    public static String generatePasswordHash(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        int iterations = 100;
        char[] chars = password.toCharArray();
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return iterations + toHex(salt) + toHex(hash);
    }

    /**
     * Utility method to check that a plain text password matches a hashed password.
     * @param stored The hash of the password.
     * @param toCheck The plaintext password to check against the hashed.
     * @return True if the passwords match, false otherwise.
     */
    public static boolean checkPasswordHash(String stored, String toCheck) throws NoSuchAlgorithmException, InvalidKeySpecException{
        String part0 = stored.substring(0, 3);
        String part1 = stored.substring(3, 35);
        String part2 = stored.substring(35);
        int iterations = Integer.parseInt(part0);
        byte[] salt = fromHex(part1);
        byte[] hash = fromHex(part2);

        PBEKeySpec spec = new PBEKeySpec(toCheck.toCharArray(), salt, iterations, hash.length * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] testHash = skf.generateSecret(spec).getEncoded();

        int diff = hash.length ^ testHash.length;
        for(int i = 0; i < hash.length && i < testHash.length; i++)
        {
            diff |= hash[i] ^ testHash[i];
            if(hash[i] != testHash[i]){
                int cx = 0;
            }
        }
        return diff == 0;
    }

    private static byte[] fromHex(String hex)
    {
        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++)
        {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    private static String toHex(byte[] array)
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }

    /**
     * This method receives an array of strings and pairs consecutive parameters as key, value pairs.
     * For example, calling toProperties('prop1', 'value1', 'prop2', 'value2') would create a property map with two
     * entries.  The first entry will be 'prop1' -> 'value1' and the second will be 'prop2' -> 'value2'. An
     * IllegalArgumentException will be thrown if any value is null or there is an odd number of values, as this will
     * lead to errors in processing the parameters.
     * @param pairs Array of string values to convert to a HashMap
     * @return A HashMap of the given pairs
     */
    public static HashMap<String, String> toProperties(String ... pairs) {
        HashMap<String, String> props = new HashMap<>();
        for(int i = 0; i < pairs.length-1; i++) {
            props.put(pairs[i], pairs[++i]);
        }
        return props;
    }
}
