package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class PolicyClassNameExistsException extends PmException {
    public PolicyClassNameExistsException(String name) {
        super(ApiResponseCodes.ERR_POLICY_NAME_EXISTS, String.format("A Policy Class already exists with the name %s", name));
    }
}
