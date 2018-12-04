package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.exceptions.ErrorCodes;

public class NodeNameExistsException extends PMException {
    public NodeNameExistsException(String name) {
        super(ErrorCodes.ERR_NODE_NAME_EXISTS, String.format("A node with the name '%s' already exists", name));
    }
}
