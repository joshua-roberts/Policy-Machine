package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionsDAO;
import gov.nist.csd.pm.pap.sessions.SessionsDAO;
import gov.nist.csd.pm.pdp.engine.Decider;
import gov.nist.csd.pm.pdp.engine.PReviewDecider;

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
    public Service(String sessionID, long processID) throws PMException {
        if(sessionID == null || sessionID.isEmpty()) {
            throw new PMException(Errors.ERR_NULL_SESSION, "The session ID cannot be null or empty");
        }

        this.sessionID = sessionID;
        this.processID = processID;
    }

    protected Service() {}

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

    Graph getGraphDB() throws PMException {
        return getPAP().getGraphDB();
    }

    Graph getGraphMem() throws PMException {
        return getPAP().getGraphMem();
    }

    Search getSearch() throws PMException {
        return getPAP().getSearch();
    }

    ProhibitionsDAO getProhibitionsDB() throws PMException {
        return getPAP().getProhibitionsDB();
    }

    ProhibitionsDAO getProhibitionsMem() throws PMException {
        return getPAP().getProhibitionsMem();
    }

    SessionsDAO getSessionsDB() throws PMException {
        return getPAP().getSessionsDB();
    }

    SessionsDAO getSessionsMem() throws PMException {
        return getPAP().getSessionsMem();
    }

    /**
     * Get the ID of the User that is associated with the current session ID.
     * @return The ID of the user node.
     */
    public long getSessionUserID() throws PMException {
        return getPAP().getSessionsMem().getSessionUserID(sessionID);
    }

    public Decider newPolicyDecider() throws PMException {
        return new PReviewDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
    }
}
