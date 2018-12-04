package gov.nist.csd.pm.common.exceptions;

import static gov.nist.csd.pm.common.exceptions.ErrorCodes.ERR_NULL_OPERATIONS;

public class NullOperationsException extends PMException {
    public NullOperationsException() {
        super(ERR_NULL_OPERATIONS, "the operation set was null");
    }
}
