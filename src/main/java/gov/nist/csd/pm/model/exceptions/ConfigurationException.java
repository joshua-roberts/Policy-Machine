package gov.nist.csd.pm.model.exceptions;

public class ConfigurationException extends PmException {
    public ConfigurationException(String message) {
        super(ErrorCodes.ERR_CONFIGURATION, message);
    }
}
