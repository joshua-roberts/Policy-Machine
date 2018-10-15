package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class NoProcessFoundException extends PmException {
    public NoProcessFoundException() {
        super(ApiResponseCodes.ERR_NO_PROCESS, "There was no process provided to the PM");
    }
}