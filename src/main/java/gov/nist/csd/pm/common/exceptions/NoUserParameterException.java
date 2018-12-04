package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class NoUserParameterException extends PMException {
    public NoUserParameterException() {
        super(ErrorCodes.ERR_NO_USER_PARAMETER, "No user or user attribute was specified in the parameters, but one is required.");
    }
}
