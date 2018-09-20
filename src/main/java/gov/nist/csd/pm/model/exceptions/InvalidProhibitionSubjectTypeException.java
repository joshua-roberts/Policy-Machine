package gov.nist.csd.pm.model.exceptions;

public class InvalidProhibitionSubjectTypeException extends PmException {
    public InvalidProhibitionSubjectTypeException(String type){
        super(ErrorCodes.ERR_INVALID_PROHIBITION_SUBJECT_TYPE, "Provided ProhibitionSubjectType '" + type + "' is not one of (UA, U, P)");
    }
}