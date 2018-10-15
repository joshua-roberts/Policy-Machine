package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class ProhibitionNameExistsException extends PmException {
    public ProhibitionNameExistsException(String prohibitionName) {
        super(ApiResponseCodes.ERR_PROHIBITION_NAME_EXISTS, String.format("Prohibition with name '%s' already exists", prohibitionName));
    }
}
