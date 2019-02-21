package gov.nist.csd.pm.common.exceptions;

public class PMGraphException extends PMException {
    public PMGraphException(String msg) {
        super(Errors.ERR_GRAPH, msg);
    }
}
