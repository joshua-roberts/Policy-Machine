package gov.nist.policyserver.obligations.exceptions;

import gov.nist.policyserver.common.Constants;
import gov.nist.policyserver.exceptions.PmException;

public class NoProcessFoundException extends PmException {
    public NoProcessFoundException() {
        super(Constants.ERR_NO_PROCESS, "There was no process provided to the PM");
    }
}