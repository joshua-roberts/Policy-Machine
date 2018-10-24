package gov.nist.csd.pm.model.exceptions;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_NULL_NODE_CTX;

public class NullNodeCtxException extends PMException {

    public NullNodeCtxException() {
        super(ERR_NULL_NODE_CTX, "node context was null");
    }
}
