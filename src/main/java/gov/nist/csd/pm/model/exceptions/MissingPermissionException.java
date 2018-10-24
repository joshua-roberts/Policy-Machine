package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.exceptions.ErrorCodes;

public class MissingPermissionException extends PMException {
    public MissingPermissionException(long nodeID, String permission) {
        super(ErrorCodes.ERR_MISSING_PERMISSIONS, String.format("missing permission %s on %d", permission, nodeID));
    }
}
