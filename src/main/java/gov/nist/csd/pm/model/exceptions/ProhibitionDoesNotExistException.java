package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class ProhibitionDoesNotExistException extends PmException {
    public ProhibitionDoesNotExistException(String prohibitionName) {
        super(ApiResponseCodes.ERR_PROHIBITION_DOES_NOT_EXIST, String.format("Prohibition with name '%s' does not exist", prohibitionName));
    }
}

