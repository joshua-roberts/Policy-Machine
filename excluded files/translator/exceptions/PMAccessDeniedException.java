package gov.nist.csd.pm.demos.ndac.translator.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;
import gov.nist.csd.pm.model.exceptions.PMException;

public class PMAccessDeniedException extends PMException {
    public PMAccessDeniedException(String nodeName){
        super(ApiResponseCodes.ERR_ACCESS_DENIED, "The node \"" + nodeName + "\" is inaccessible");
    }
    public PMAccessDeniedException(long nodeID){
        super(ApiResponseCodes.ERR_ACCESS_DENIED, "The node \"" + nodeID + "\" is inaccessible");
    }
}
