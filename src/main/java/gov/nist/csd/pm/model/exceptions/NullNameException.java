package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class NullNameException extends PmException {
    public NullNameException() {
        super(ErrorCodes.ERR_NULL_NAME, "The server received a null name");
    }
}
