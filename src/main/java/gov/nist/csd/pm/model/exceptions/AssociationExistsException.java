package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.exceptions.ErrorCodes;

public class AssociationExistsException extends PMException {
    public AssociationExistsException(long uaId, long targetId) {
        super(ErrorCodes.ERR_ASSOCIATION_EXISTS, String.format("An association between %d and %d already exists.", uaId, targetId));
    }
}
