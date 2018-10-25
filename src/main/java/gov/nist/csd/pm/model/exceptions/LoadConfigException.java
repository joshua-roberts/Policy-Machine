package gov.nist.csd.pm.model.exceptions;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_LOADING_DB_CONFIG_PROPS;

public class LoadConfigException extends PMException {
    public LoadConfigException() {
        super(ERR_LOADING_DB_CONFIG_PROPS, "an error occurred when loading the database configuration properties.");
    }
}
