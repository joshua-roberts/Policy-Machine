package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class InvalidProhibitionSubjectTypeException extends PmException {
    public InvalidProhibitionSubjectTypeException(String type){
        super(ApiResponseCodes.ERR_INVALID_PROHIBITION_SUBJECT_TYPE, "Provided ProhibitionSubjectType '" + type + "' is not one of (UA, U, P)");
    }
}