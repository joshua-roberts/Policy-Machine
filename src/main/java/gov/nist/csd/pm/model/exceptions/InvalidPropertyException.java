package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.exceptions.ErrorCodes;

public class InvalidPropertyException extends PMException {
    public InvalidPropertyException(String key, String value){
        super(ErrorCodes.ERR_INVALID_PROPERTY, "The property '" + key + "=" + value + "' is invalid");
    }

    public InvalidPropertyException(String message){
        super(ErrorCodes.ERR_INVALID_PROPERTY, message);
    }
}