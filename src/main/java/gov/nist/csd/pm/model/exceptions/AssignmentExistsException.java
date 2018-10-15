package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class AssignmentExistsException extends PmException {
    private static final String ASSIGNMENT_EXISTS_ERRMSG = "There is already an assignment between %d (start) and %d (end)";

    public AssignmentExistsException(String message) {
        super(ApiResponseCodes.ERR_ASSIGNMENT_EXISTS, message);
    }

    public AssignmentExistsException(Integer start, Integer end) {
        super(ApiResponseCodes.ERR_ASSIGNMENT_EXISTS, String.format(
                ASSIGNMENT_EXISTS_ERRMSG, start, end));
    }
}
