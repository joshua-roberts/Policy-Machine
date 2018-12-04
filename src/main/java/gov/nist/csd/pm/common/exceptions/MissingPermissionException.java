package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

import static gov.nist.csd.pm.common.exceptions.ErrorCodes.ERR_MISSING_PERMISSIONS;

public class MissingPermissionException extends PMException {
    public MissingPermissionException(long nodeID, String permission) {
        super(ERR_MISSING_PERMISSIONS, String.format("missing permission %s on %d", permission, nodeID));
    }

    public MissingPermissionException(String msg) {
        super(ERR_MISSING_PERMISSIONS, msg);
    }
}
