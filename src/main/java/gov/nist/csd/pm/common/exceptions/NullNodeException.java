package gov.nist.csd.pm.common.exceptions;

import static gov.nist.csd.pm.common.exceptions.ErrorCodes.ERR_NULL_NODE_CTX;

public class NullNodeException extends PMException {

    public NullNodeException() {
        super(ERR_NULL_NODE_CTX, "node was null");
    }
}
