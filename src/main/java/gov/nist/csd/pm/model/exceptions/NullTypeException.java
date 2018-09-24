package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class NullTypeException extends PmException {
    public NullTypeException() {
        super(ErrorCodes.ERR_NULL_TYPE, "The server received a null type");
    }
}
