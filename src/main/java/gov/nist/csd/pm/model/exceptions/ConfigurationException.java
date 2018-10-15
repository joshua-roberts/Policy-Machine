package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class ConfigurationException extends PmException {
    public ConfigurationException(String message) {
        super(ApiResponseCodes.ERR_CONFIGURATION, message);
    }
}
