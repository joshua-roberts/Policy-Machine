package gov.nist.csd.pm.pdp.engine;

import gov.nist.csd.pm.common.exceptions.*;

import java.util.HashSet;

public interface ProhibitionDecider {
    /**
     * List the permissions that are prohibited on the target node for the subject.  The subject can be the ID of a user
     * or a process.
     * @param subjectID The ID of the subject, either a user or a process.
     * @param targetID The ID of the target to get the prohibited permissions on.
     * @return The set of permissions that are denied for the subject on the target.
     * @throws PMException If there is an error listing the prohibited permissions on the target.
     */
    HashSet<String> listProhibitedPermissions(long subjectID, long targetID) throws PMException;
}
