package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class NullTypeException extends PMException {
    public NullTypeException() {
        super(ErrorCodes.ERR_NULL_TYPE, "The server received a null type");
    }
}
