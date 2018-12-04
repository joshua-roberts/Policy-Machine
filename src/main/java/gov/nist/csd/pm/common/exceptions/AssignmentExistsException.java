package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class AssignmentExistsException extends PMException {
    private static final String ASSIGNMENT_EXISTS_ERRMSG = "There is already an assignment between %d (start) and %d (end)";

    public AssignmentExistsException(String message) {
        super(ErrorCodes.ERR_ASSIGNMENT_EXISTS, message);
    }

    public AssignmentExistsException(Integer start, Integer end) {
        super(ErrorCodes.ERR_ASSIGNMENT_EXISTS, String.format(
                ASSIGNMENT_EXISTS_ERRMSG, start, end));
    }
}
