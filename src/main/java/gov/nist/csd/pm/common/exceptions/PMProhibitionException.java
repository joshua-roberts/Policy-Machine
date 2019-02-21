package gov.nist.csd.pm.common.exceptions;

public class PMProhibitionException  extends PMException {
    public PMProhibitionException(String msg) {
        super(Errors.ERR_PROHIBITION, msg);
    }
}
