package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class InvalidProhibitionSubjectTypeException extends PMException {
    public InvalidProhibitionSubjectTypeException(String type){
        super(ErrorCodes.ERR_INVALID_PROHIBITION_SUBJECT_TYPE, "Provided ProhibitionSubjectType '" + type + "' is not one of (UA, U, P)");
    }
}