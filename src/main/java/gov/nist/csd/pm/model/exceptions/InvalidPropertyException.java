package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class InvalidPropertyException extends PmException {
    public InvalidPropertyException(String key, String value){
        super(ErrorCodes.ERR_INVALID_PROPERTY, "The property '" + key + "=" + value + "' is invalid");
    }

    public InvalidPropertyException(String message){
        super(ErrorCodes.ERR_INVALID_PROPERTY, message);
    }
}