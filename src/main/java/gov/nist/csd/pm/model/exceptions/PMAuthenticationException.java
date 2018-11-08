package gov.nist.csd.pm.model.exceptions;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_AUTH;

public class PMAuthenticationException extends PMException {
    public PMAuthenticationException() {
        super(ERR_AUTH, "password was incorrect");
    }
}
