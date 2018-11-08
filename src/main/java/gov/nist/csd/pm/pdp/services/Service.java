package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Graph;
import gov.nist.csd.pm.model.graph.Search;
import gov.nist.csd.pm.model.prohibitions.Prohibition;
import gov.nist.csd.pm.model.prohibitions.ProhibitionsDAO;
import gov.nist.csd.pm.pap.graph.MemGraph;
import gov.nist.csd.pm.pap.sessions.SessionsDAO;
import gov.nist.csd.pm.pdp.engine.MemPolicyDecider;
import gov.nist.csd.pm.pdp.engine.PolicyDecider;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import static gov.nist.csd.pm.pap.PAP.getPAP;

/**
 * Class to provide common methods to all services.
 */
public class Service {

    /**
     * The ID of the session currently using the service.
     */
    private String sessionID;

    /**
     * The ID of the process currently using the service.
     */
    private long processID;

    /**
     * Create a new Service with a sessionID and processID from the request context.
     * @param sessionID The ID of the current session. This cannot be null or empty.
     * @param processID The ID of the current process. This can be 0.
     * @throws IllegalArgumentException If the session ID provided by the request context is null or empty
     */
    public Service(String sessionID, long processID) {
        if(sessionID == null || sessionID.isEmpty()) {
            throw new IllegalArgumentException("The session ID cannot be null or empty");
        }

        this.sessionID = sessionID;
        this.processID = processID;
    }

    /**
     * Get the ID of the current session.
     * @return The current session's ID.
     */
    protected String getSessionID() {
        return sessionID;
    }

    /**
     * Get the ID of the current process.  The current process can be 0, in which case a process
     * is not currently being used.
     * @return the ID of the current process.
     */
    long getProcessID() {
        return processID;
    }


    // The following methods are getter methods for the PAP.

    public Graph getGraphDB() throws InvalidProhibitionSubjectTypeException, LoaderException, LoadConfigException, DatabaseException {
        return getPAP().getGraphDB();
    }

    public Graph getGraphMem() throws InvalidProhibitionSubjectTypeException, LoaderException, LoadConfigException, DatabaseException {
        return getPAP().getGraphMem();
    }

    public Search getSearch() throws InvalidProhibitionSubjectTypeException, LoaderException, LoadConfigException, DatabaseException {
        return getPAP().getSearch();
    }

    public ProhibitionsDAO getProhibitionsDB() throws InvalidProhibitionSubjectTypeException, LoaderException, LoadConfigException, DatabaseException {
        return getPAP().getProhibitionsDB();
    }

    public ProhibitionsDAO getProhibitionsMem() throws InvalidProhibitionSubjectTypeException, LoaderException, LoadConfigException, DatabaseException {
        return getPAP().getProhibitionsMem();
    }

    public SessionsDAO getSessionsDB() throws InvalidProhibitionSubjectTypeException, LoaderException, LoadConfigException, DatabaseException {
        return getPAP().getSessionsDB();
    }

    public SessionsDAO getSessionsMem() throws InvalidProhibitionSubjectTypeException, LoaderException, LoadConfigException, DatabaseException {
        return getPAP().getSessionsMem();
    }

    /**
     * Get the ID of the User that is associated with the current session ID.
     * @return The ID of the user node.
     */
    public long getSessionUserID() throws LoaderException, DatabaseException, LoadConfigException, SessionDoesNotExistException, InvalidProhibitionSubjectTypeException {
        return getPAP().getSessionsMem().getSessionUserID(sessionID);
    }

    public PolicyDecider newPolicyDecider() throws LoadConfigException, DatabaseException, LoaderException, InvalidProhibitionSubjectTypeException {
        return new MemPolicyDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
    }

    /**
     * Utility method to hash a password. This will be used by the session and node service classes.
     * @param password The plaintext password to hash.
     * @return The hash of the password;
     */
    static String generatePasswordHash(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
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
    static boolean checkPasswordHash(String stored, String toCheck) throws NoSuchAlgorithmException, InvalidKeySpecException{
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
}
