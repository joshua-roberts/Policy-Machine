package gov.nist.csd.pm.model.exceptions;

public class InvalidNodeTypeException extends PmException {
    public InvalidNodeTypeException(String type){
        super(ErrorCodes.ERR_INVALID_NODETYPE, "Provided NodeType '" + type + "' is not one of (C, OA, UA, U, O, PC, D, OS)");
    }
    public InvalidNodeTypeException(int type){
        super(ErrorCodes.ERR_INVALID_NODETYPE, "Provided NodeType ID " + type + " is not between 1-7");
    }
}