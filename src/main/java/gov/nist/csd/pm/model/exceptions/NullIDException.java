package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class NullIDException extends PmException {
    public NullIDException() {
        super(ApiResponseCodes.ERR_NULL_ID, "The server received a null id");
    }
}