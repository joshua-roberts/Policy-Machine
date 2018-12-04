package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class ProhibitionResourceDoesNotExistException extends PMException {
    public ProhibitionResourceDoesNotExistException(String prohibtionName, long resourceId) {
        super(ErrorCodes.ERR_PROHIBITION_RESOURCE_DOES_NOT_EXIST, String.format("Prohibition with name '%s' does not have a resource with id %d", prohibtionName, resourceId));
    }
}



