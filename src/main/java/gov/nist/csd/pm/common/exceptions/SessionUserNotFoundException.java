package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class SessionUserNotFoundException extends PMException {
    public SessionUserNotFoundException(String session) {
        super(ErrorCodes.ERR_SESSION_USER_NOT_FOUND, "Could not find a user for session " + session);
    }
}

