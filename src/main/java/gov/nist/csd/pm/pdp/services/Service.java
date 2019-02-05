package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionsDAO;
import gov.nist.csd.pm.pap.graph.GraphPAP;
import gov.nist.csd.pm.pap.prohibitions.ProhibitionsPAP;
import gov.nist.csd.pm.pap.sessions.SessionManager;
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
    private long userID;

    /**
     * The ID of the process currently using the service.
     */
    private long processID;

    /**
     * Create a new Service with a sessionID and processID from the request context.
     * @param userID The ID of the user.
     * @param processID The ID of the current process. This can be 0.
     * @throws IllegalArgumentException If the user ID provided is 0.
     */
    public Service(long userID, long processID) throws PMException {
        if(userID == 0) {
            throw new PMException(Errors.ERR_NO_USER_PARAMETER, "no user or a user ID of 0 was provided to the PDP service");
        }

        this.userID = userID;
        this.processID = processID;
    }

    protected Service() {}

    /**
     * Get the ID of the current session.
     * @return The current session's ID.
     */
    protected long getUserID() {
        return userID;
    }

    /**
     * Get the ID of the current process.  The current process can be 0, in which case a process
     * is not currently being used.
     * @return the ID of the current process.
     */
    public long getProcessID() {
        return processID;
    }

    protected GraphPAP getGraphPAP() throws PMException {
        return getPAP().getGraphPAP();
    }

    protected ProhibitionsPAP getProhibitionsPAP() throws PMException {
        return getPAP().getProhibitionsPAP();
    }

    SessionManager getSessionManager() throws PMException {
        return getPAP().getSessionManager();
    }

    public Decider getDecider() throws PMException {
        return new PReviewDecider(getGraphPAP(), getProhibitionsPAP().getProhibitions());
    }
}
