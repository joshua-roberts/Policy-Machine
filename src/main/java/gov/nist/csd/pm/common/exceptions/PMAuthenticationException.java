package gov.nist.csd.pm.common.exceptions;

import static gov.nist.csd.pm.common.exceptions.ErrorCodes.ERR_AUTH;

public class PMAuthenticationException extends PMException {
    public PMAuthenticationException() {
        super(ERR_AUTH, "password was incorrect");
    }
}
