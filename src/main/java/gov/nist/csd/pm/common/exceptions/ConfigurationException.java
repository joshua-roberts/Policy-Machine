package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class ConfigurationException extends PMException {
    public ConfigurationException(String message) {
        super(ErrorCodes.ERR_CONFIGURATION, message);
    }
}
