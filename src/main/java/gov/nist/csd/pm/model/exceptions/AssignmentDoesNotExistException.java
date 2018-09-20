package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class AssignmentDoesNotExistException extends PmException {
    public AssignmentDoesNotExistException(long childID, long parentID) {
        super(ErrorCodes.ERR_ASSIGNMENT_DOES_NOT_EXIST, String.format("An assignment between %d and %d does not exist.", childID, parentID));
    }
}
