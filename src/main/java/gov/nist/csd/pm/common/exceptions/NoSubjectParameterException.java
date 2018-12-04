package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class NoSubjectParameterException extends PMException {
    public NoSubjectParameterException() {
        super(ErrorCodes.ERR_NO_SUBJECT_PARAMETER, "No user or process was specified in the parameters, but one is required.");
    }
}