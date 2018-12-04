package gov.nist.csd.pm.common.exceptions;

import static gov.nist.csd.pm.common.exceptions.ErrorCodes.ERR_HASHING_USER_PSWD;

public class HashingUserPasswordException extends PMException {
    public HashingUserPasswordException() {
        super(ERR_HASHING_USER_PSWD, "error hashing user password");
    }
}
