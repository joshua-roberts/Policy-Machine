package gov.nist.csd.pm.common.exceptions;

public class PMAuthorizationException extends PMException {
    public PMAuthorizationException(String msg) {
        super(Errors.ERR_AUTHORIZATION, msg);
    }
}
