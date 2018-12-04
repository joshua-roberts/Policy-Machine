package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class ProhibitionSubjectDoesNotExistException extends PMException {
    public ProhibitionSubjectDoesNotExistException(String prohibitionName) {
        super(ErrorCodes.ERR_PROHIBITION_SUBJECT_DOES_NOT_EXIST, String.format("There is no ProhibitionSubject associated with prohibitions '%s'", prohibitionName));
    }
}
