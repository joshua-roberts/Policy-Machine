package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.exceptions.ErrorCodes;

public class UnexpectedNumberOfNodesException extends PMException {
    public UnexpectedNumberOfNodesException() {
        super(ErrorCodes.ERR_UNEXPECTED_NUMBER_OF_NODES, "Expected one node but found multiple or none.");
    }
}
