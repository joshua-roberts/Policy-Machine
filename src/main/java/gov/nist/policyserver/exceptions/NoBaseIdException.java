package gov.nist.policyserver.exceptions;

import gov.nist.policyserver.common.Constants;

public class NoBaseIdException extends PmException {
    public NoBaseIdException() {
        super(Constants.ERR_NO_BASE_ID, "The serve received a null base ID.  You Can only create a Policy Class node without specifying a base node.  All other types must be created in an existing node.");
    }
}