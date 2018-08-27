package gov.nist.csd.pm.model.exceptions;

import gov.nist.policyserver.common.Constants;

public class MissingPermissionException extends PmException {
    public MissingPermissionException(String message) {
        super(Constants.ERR_MISSING_PERMISSIONS, message);
    }
}
