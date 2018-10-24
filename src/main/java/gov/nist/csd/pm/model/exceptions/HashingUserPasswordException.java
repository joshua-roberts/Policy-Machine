package gov.nist.csd.pm.model.exceptions;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_HASHING_USER_PSWD;

public class HashingUserPasswordException extends PMException {
    public HashingUserPasswordException() {
        super(ERR_HASHING_USER_PSWD, "error hashing user password");
    }
}
