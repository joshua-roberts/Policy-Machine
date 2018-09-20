package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class UnexpectedNumberOfNodesException extends PmException {
    public UnexpectedNumberOfNodesException() {
        super(ErrorCodes.ERR_UNEXPECTED_NUMBER_OF_NODES, "Expected one node but found multiple or none.");
    }
}
