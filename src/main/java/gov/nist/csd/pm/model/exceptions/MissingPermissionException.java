package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class MissingPermissionException extends PmException {
    public MissingPermissionException(String message) {
        super(ApiResponseCodes.ERR_MISSING_PERMISSIONS, message);
    }
}
