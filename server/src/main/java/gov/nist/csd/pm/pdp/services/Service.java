package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.decider.Decider;
import gov.nist.csd.pm.decider.PReviewDecider;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.Graph;
import gov.nist.csd.pm.pap.GraphPAP;
import gov.nist.csd.pm.pap.PAP;
import gov.nist.csd.pm.pap.ProhibitionsPAP;
import gov.nist.csd.pm.pep.sessions.Sessions;
import gov.nist.csd.pm.prohibitions.ProhibitionsDAO;

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
     * @param userID the ID of the user.
     * @param processID the ID of the current process. This can be 0.
     * @throws IllegalArgumentException if the user ID provided is 0.
     */
    public Service(long userID, long processID) throws PMGraphException {
        if(userID == 0) {
            throw new PMGraphException("no user or a user ID of 0 was provided to the PDP service");
        }

        this.userID = userID;
        this.processID = processID;
    }

    protected Service() {}

    /**
     * Get the ID of the current session.
     * @return the current session's ID.
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

    Graph getGraphPAP() throws PMException {
        return PAP.getPAP().getGraphPAP();
    }

    ProhibitionsDAO getProhibitionsPAP() throws PMException {
        return PAP.getPAP().getProhibitionsPAP();
    }

    public Decider getDecider() throws PMException {
        return new PReviewDecider(getGraphPAP());
    }
}
