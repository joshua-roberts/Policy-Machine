package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class SessionUserNotFoundException extends PmException {
    public SessionUserNotFoundException(String session) {
        super(Constants.ERR_SESSION_USER_NOT_FOUND, "Could not find a user for session " + session);
    }
}

