package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class NoUserParameterException extends PmException {
    public NoUserParameterException() {
        super(Constants.ERR_NO_USER_PARAMETER, "No user or user attribute was specified in the parameters, but one is required.");
    }
}
