package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class ProhibitionResourceExistsException extends PmException {
    public ProhibitionResourceExistsException(String prohibtionName, long resourceId) {
        super(Constants.ERR_PROHIBITION_RESOURCE_EXISTS, String.format("Prohibition with name '%s' already has a resource with id %d", prohibtionName, resourceId));
    }
}
