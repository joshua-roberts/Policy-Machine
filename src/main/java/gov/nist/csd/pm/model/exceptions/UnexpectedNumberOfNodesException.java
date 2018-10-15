package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class UnexpectedNumberOfNodesException extends PmException {
    public UnexpectedNumberOfNodesException() {
        super(ApiResponseCodes.ERR_UNEXPECTED_NUMBER_OF_NODES, "Expected one node but found multiple or none.");
    }
}
