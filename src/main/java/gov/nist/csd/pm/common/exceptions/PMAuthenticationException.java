package gov.nist.csd.pm.common.exceptions;

public class PMAuthenticationException extends PMException {
    public PMAuthenticationException(String msg) {
        super(Errors.ERR_AUTHENTICATION, msg);
    }
}
