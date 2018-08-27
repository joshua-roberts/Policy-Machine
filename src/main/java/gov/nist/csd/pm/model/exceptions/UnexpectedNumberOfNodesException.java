package gov.nist.csd.pm.model.exceptions;

import gov.nist.policyserver.common.Constants;

public class UnexpectedNumberOfNodesException extends PmException {
    public UnexpectedNumberOfNodesException() {
        super(Constants.ERR_UNEXPECTED_NUMBER_OF_NODES, "Expected one node but found multiple or none.");
    }
}
