package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;
import gov.nist.csd.pm.model.exceptions.ErrorCodes;
import gov.nist.csd.pm.model.exceptions.PmException;

public class NoProcessFoundException extends PmException {
    public NoProcessFoundException() {
        super(ErrorCodes.ERR_NO_PROCESS, "There was no process provided to the PM");
    }
}