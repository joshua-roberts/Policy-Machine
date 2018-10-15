package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class AssociationDoesNotExistException extends PmException {
    public AssociationDoesNotExistException(long uaId, long targetId) {
        super(ApiResponseCodes.ERR_ASSOCIATION_DOES_NOT_EXIST, String.format("An association between %d and %d does not exist.", uaId, targetId));
    }
}
