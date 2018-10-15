package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.pep.response.ApiResponseCodes;

public class InvalidNodeTypeException extends PmException {
    public InvalidNodeTypeException(String type){
        super(ApiResponseCodes.ERR_INVALID_NODETYPE, "Provided NodeType '" + type + "' is not one of (C, OA, UA, U, O, PC, D, OS)");
    }
    public InvalidNodeTypeException(int type){
        super(ApiResponseCodes.ERR_INVALID_NODETYPE, "Provided NodeType ID " + type + " is not between 1-7");
    }
}