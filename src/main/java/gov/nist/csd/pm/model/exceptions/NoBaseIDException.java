package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class NoBaseIDException extends PmException {
    public NoBaseIDException() {
        super(ErrorCodes.ERR_NO_BASE_ID, "The serve received a null base ID.  You Can only create a Policy Class node without specifying a base node.  All other types must be created in an existing node.");
    }
}