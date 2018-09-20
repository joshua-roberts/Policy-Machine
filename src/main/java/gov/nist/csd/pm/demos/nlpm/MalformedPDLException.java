package gov.nist.csd.pm.demos.nlpm;

import gov.nist.csd.pm.model.exceptions.ErrorCodes;
import gov.nist.csd.pm.model.exceptions.PmException;

public class MalformedPDLException extends PmException {
    public MalformedPDLException(String command) {
        super(ErrorCodes.ERR_MALFORMED_PDL, command);
    }
}
