package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class ProhibitionNameExistsException extends PmException {
    public ProhibitionNameExistsException(String prohibitionName) {
        super(ErrorCodes.ERR_PROHIBITION_NAME_EXISTS, String.format("Prohibition with name '%s' already exists", prohibitionName));
    }
}
