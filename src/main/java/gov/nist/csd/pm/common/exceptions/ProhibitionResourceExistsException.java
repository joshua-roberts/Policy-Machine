package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class ProhibitionResourceExistsException extends PMException {
    public ProhibitionResourceExistsException(String prohibtionName, long resourceId) {
        super(ErrorCodes.ERR_PROHIBITION_RESOURCE_EXISTS, String.format("Prohibition with name '%s' already has a resource with id %d", prohibtionName, resourceId));
    }
}
