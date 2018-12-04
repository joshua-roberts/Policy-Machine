package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class SessionDoesNotExistException extends PMException {
    public SessionDoesNotExistException(String sessionId){
        super(ErrorCodes.ERR_SESSION_DOES_NOT_EXIST, String.format("Session with id %s does not exist", sessionId));
    }
}
