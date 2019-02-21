package gov.nist.csd.pm.common.exceptions;

public class PMDBException extends PMException {
    public PMDBException(String msg) {
        super(Errors.ERR_DB, msg);
    }
}
