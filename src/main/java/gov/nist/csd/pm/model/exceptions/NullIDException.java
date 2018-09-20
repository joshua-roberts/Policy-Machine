package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class NullIDException extends PmException {
    public NullIDException() {
        super(ErrorCodes.ERR_NULL_ID, "The server received a null id");
    }
}