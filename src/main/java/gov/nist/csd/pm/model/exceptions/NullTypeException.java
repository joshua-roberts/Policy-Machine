package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class NullTypeException extends PmException {
    public NullTypeException() {
        super(ApiResponseCodes.ERR_NULL_TYPE, "The server received a null type");
    }
}
