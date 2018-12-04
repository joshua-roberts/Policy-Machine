package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionsDAO;
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
    public long getProcessID() {
        return processID;
    }


    // The following methods are getter methods for the PAP.

    Graph getGraphDB() throws InvalidProhibitionSubjectTypeException, LoadConfigException, DatabaseException {
        return getPAP().getGraphDB();
    }

    Graph getGraphMem() throws InvalidProhibitionSubjectTypeException, LoadConfigException, DatabaseException {
        return getPAP().getGraphMem();
    }

    Search getSearch() throws InvalidProhibitionSubjectTypeException, LoadConfigException, DatabaseException {
        return getPAP().getSearch();
    }

    ProhibitionsDAO getProhibitionsDB() throws InvalidProhibitionSubjectTypeException, LoadConfigException, DatabaseException {
        return getPAP().getProhibitionsDB();
    }

    ProhibitionsDAO getProhibitionsMem() throws InvalidProhibitionSubjectTypeException, LoadConfigException, DatabaseException {
        return getPAP().getProhibitionsMem();
    }

    SessionsDAO getSessionsDB() throws InvalidProhibitionSubjectTypeException, LoadConfigException, DatabaseException {
        return getPAP().getSessionsDB();
    }

    SessionsDAO getSessionsMem() throws InvalidProhibitionSubjectTypeException, LoadConfigException, DatabaseException {
        return getPAP().getSessionsMem();
    }

    /**
     * Get the ID of the User that is associated with the current session ID.
     * @return The ID of the user node.
     */
    public long getSessionUserID() throws DatabaseException, LoadConfigException, SessionDoesNotExistException, InvalidProhibitionSubjectTypeException {
        return getPAP().getSessionsMem().getSessionUserID(sessionID);
    }

    public PolicyDecider newPolicyDecider() throws LoadConfigException, DatabaseException, InvalidProhibitionSubjectTypeException {
        return new MemPolicyDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
    }
}
