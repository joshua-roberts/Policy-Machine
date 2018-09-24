package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.pdp.analytics.PmAnalytics;
import gov.nist.csd.pm.pip.dao.DAOManager;
import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.pip.graph.PmGraph;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

public class Service {
    public PmGraph getGraph() throws ClassNotFoundException, SQLException, InvalidPropertyException, IOException, DatabaseException {
        return getDaoManager().getGraphDAO().getGraph();
    }

    public PmAnalytics getAnalytics() throws ClassNotFoundException, SQLException, InvalidPropertyException, IOException, DatabaseException {
        return getDaoManager().getGraphDAO().getAnalytics();
    }

    public DAOManager getDaoManager() throws ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        return DAOManager.getDaoManager();
    }

    public Node getSessionUser(String session) throws SessionUserNotFoundException, SessionDoesNotExistException, ClassNotFoundException, SQLException, InvalidPropertyException, IOException, DatabaseException {
        long sessionUserID = getDaoManager().getSessionsDAO().getSessionUserID(session);
        Node node = getGraph().getNode(sessionUserID);
        if(node == null) {
            throw new SessionUserNotFoundException(session);
        }

        return node;
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
