package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class InvalidPropertyException extends PmException {
    public InvalidPropertyException(String key, String value){
        super(ApiResponseCodes.ERR_INVALID_PROPERTY, "The property '" + key + "=" + value + "' is invalid");
    }

    public InvalidPropertyException(String message){
        super(ApiResponseCodes.ERR_INVALID_PROPERTY, message);
    }
}