package gov.nist.csd.pm.demos.ndac.translator.exceptions;

import gov.nist.csd.pm.model.exceptions.ErrorCodes;
import gov.nist.csd.pm.model.exceptions.PMException;

public class PMAccessDeniedException extends PMException {
    public PMAccessDeniedException(String nodeName){
        super(ErrorCodes.ERR_ACCESS_DENIED, "The node \"" + nodeName + "\" is inaccessible");
    }
    public PMAccessDeniedException(long nodeID){
        super(ErrorCodes.ERR_ACCESS_DENIED, "The node \"" + nodeID + "\" is inaccessible");
    }
}
