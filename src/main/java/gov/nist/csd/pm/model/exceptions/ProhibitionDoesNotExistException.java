package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.exceptions.ErrorCodes;

public class ProhibitionDoesNotExistException extends PMException {
    public ProhibitionDoesNotExistException(String prohibitionName) {
        super(ErrorCodes.ERR_PROHIBITION_DOES_NOT_EXIST, String.format("Prohibition with name '%s' does not exist", prohibitionName));
    }
}

