package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.exceptions.ErrorCodes;

public class NullNameException extends PMException {
    public NullNameException() {
        super(ErrorCodes.ERR_NULL_NAME, "The server received a null name");
    }
}
