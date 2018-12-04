package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class NoIDException extends PMException {
    public NoIDException() {
        super(ErrorCodes.ERR_NO_ID, "Expected an ID but got 0");
    }
}