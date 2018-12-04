package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class PolicyClassNameExistsException extends PMException {
    public PolicyClassNameExistsException(String name) {
        super(ErrorCodes.ERR_POLICY_NAME_EXISTS, String.format("A Policy Class already exists with the name %s", name));
    }
}
