package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.exceptions.ErrorCodes;

public class NoProcessFoundException extends PMException {
    public NoProcessFoundException() {
        super(ErrorCodes.ERR_NO_PROCESS, "There was no process provided to the PM");
    }
}