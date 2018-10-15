package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class NoUserParameterException extends PmException {
    public NoUserParameterException() {
        super(ApiResponseCodes.ERR_NO_USER_PARAMETER, "No user or user attribute was specified in the parameters, but one is required.");
    }
}
