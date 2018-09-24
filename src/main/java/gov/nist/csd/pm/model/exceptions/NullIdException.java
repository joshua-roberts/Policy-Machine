package gov.nist.csd.pm.model.exceptions;

import gov.nist.policyserver.common.Constants;

public class NullIdException extends PmException {
    public NullIdException() {
        super(Constants.ERR_NULL_ID, "The server received a null id");
    }
}