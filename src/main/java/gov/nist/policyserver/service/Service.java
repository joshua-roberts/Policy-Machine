package gov.nist.policyserver.service;

import gov.nist.policyserver.analytics.PmAnalytics;
import gov.nist.policyserver.dao.DAOManager;
import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.graph.PmGraph;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.Property;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.HashSet;

import static gov.nist.policyserver.common.Constants.*;

public class Service {
//    private DAOManager daoManager = DAOManager.instance;

    public PmGraph getGraph() { //throws ClassNotFoundException, SQLException, IOException, DatabaseException {
        return DAOManager.instance.graphDAO.getGraph();
    }

    public PmAnalytics getAnalytics() { //throws ClassNotFoundException, SQLException, IOException, DatabaseException {
        return DAOManager.instance.getGraphDAO().getAnalytics();
    }

    public DAOManager getDaoManager() { // throws ClassNotFoundException, SQLException, DatabaseException, IOException {
        return DAOManager.instance;
    }

    public Node getSessionUser(String session) throws SessionUserNotFoundException, SessionDoesNotExistException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        long sessionUserId = DAOManager.getSessionsDAO().getSessionUserId(session);
        Node node = getGraph().getNode(sessionUserId);
        if(node == null) {
            throw new SessionUserNotFoundException(session);
        }

        return node;
    }

    public Node getConnector() throws InvalidPropertyException, ConfigurationException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        HashSet<Node> nodes = getGraph().getNodes();
        for(Node node : nodes) {
            if(node.getName().equals(CONNECTOR_NAME) && node.hasProperty(new Property(NAMESPACE_PROPERTY, CONNECTOR_NAMESPACE))) {
                return node;
            }
        }

        throw new ConfigurationException("Could not find connector node 'PM'.  Make sure to load super.pm first");
    }

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

        String x = toHex(testHash);

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
}
