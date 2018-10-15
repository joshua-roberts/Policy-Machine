package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class SessionDoesNotExistException extends PmException {
    public SessionDoesNotExistException(String sessionId){
        super(ApiResponseCodes.ERR_SESSION_DOES_NOT_EXIST, String.format("Session with id %s does not exist", sessionId));
    }
}
