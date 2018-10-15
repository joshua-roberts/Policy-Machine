package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class NullNameException extends PmException {
    public NullNameException() {
        super(ApiResponseCodes.ERR_NULL_NAME, "The server received a null name");
    }
}
