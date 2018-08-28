package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class ProhibitionSubjectDoesNotExistException extends PmException {
    public ProhibitionSubjectDoesNotExistException(String prohibitionName) {
        super(Constants.ERR_PROHIBITION_SUBJECT_DOES_NOT_EXIST, String.format("There is no ProhibitionSubject associated with prohibitions '%s'", prohibitionName));
    }
}
