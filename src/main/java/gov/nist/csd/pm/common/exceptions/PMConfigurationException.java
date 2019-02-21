package gov.nist.csd.pm.common.exceptions;

public class PMConfigurationException extends PMException {
    public PMConfigurationException(String msg) {
        super(Errors.ERR_CONFIG, msg);
    }
}
