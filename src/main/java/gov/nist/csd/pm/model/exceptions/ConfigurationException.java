package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.exceptions.ErrorCodes;

public class ConfigurationException extends PMException {
    public ConfigurationException(String message) {
        super(ErrorCodes.ERR_CONFIGURATION, message);
    }
}
