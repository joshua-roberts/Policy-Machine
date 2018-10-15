package gov.nist.csd.pm.demos.ndac.translator.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;
import gov.nist.csd.pm.model.exceptions.PmException;

public class PMAccessDeniedException extends PmException {
    public PMAccessDeniedException(String node){
        super(ApiResponseCodes.ERR_ACCESS_DENIED, "The attribute \"" + node + "\" is inaccessible");
    }
}
