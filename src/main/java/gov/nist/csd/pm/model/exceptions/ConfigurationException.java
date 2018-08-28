package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class ConfigurationException extends PmException {
    public ConfigurationException(String message) {
        super(Constants.ERR_CONFIGURATION, message);
    }
}
