package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class AssociationExistsException extends PmException {
    public AssociationExistsException(long uaId, long targetId) {
        super(ApiResponseCodes.ERR_ASSOCIATION_EXISTS, String.format("An association between %d and %d already exists.", uaId, targetId));
    }
}
