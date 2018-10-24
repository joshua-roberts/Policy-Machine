package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.exceptions.ErrorCodes;

public class NodeNotFoundException extends PMException {
    public NodeNotFoundException(long id) {
        super(ErrorCodes.ERR_NODE_NOT_FOUND, String.format("OldNode with id %d could not be found", id));
    }

    public NodeNotFoundException(String nodeName){
        super(ErrorCodes.ERR_NODE_NOT_FOUND, String.format("OldNode with name %s could not be found", nodeName));
    }
}
