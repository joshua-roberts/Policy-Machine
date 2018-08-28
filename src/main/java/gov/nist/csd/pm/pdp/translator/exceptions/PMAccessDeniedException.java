package gov.nist.csd.pm.pdp.translator.exceptions;

import gov.nist.csd.pm.model.Constants;
import gov.nist.csd.pm.model.exceptions.PmException;

public class PMAccessDeniedException extends PmException {
    public PMAccessDeniedException(String node){
        super(Constants.ERR_ACCESS_DENIED, "The attribute \"" + node + "\" is inaccessible");
    }
}
