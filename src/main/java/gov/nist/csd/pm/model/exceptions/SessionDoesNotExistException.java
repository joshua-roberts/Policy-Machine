package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class SessionDoesNotExistException extends PmException {
    public SessionDoesNotExistException(String sessionId){
        super(ErrorCodes.ERR_SESSION_DOES_NOT_EXIST, String.format("Session with id %s does not exist", sessionId));
    }
}
