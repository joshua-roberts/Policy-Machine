package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class NoSubjectParameterException extends PmException {
    public NoSubjectParameterException() {
        super(ErrorCodes.ERR_NO_SUBJECT_PARAMETER, "No user or process was specified in the parameters, but one is required.");
    }
}
