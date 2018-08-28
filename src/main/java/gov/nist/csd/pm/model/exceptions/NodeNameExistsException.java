package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.Constants;

public class NodeNameExistsException extends PmException {
    public NodeNameExistsException(String name) {
        super(Constants.ERR_NODE_NAME_EXISTS, String.format("A node with the name '%s' already exists", name));
    }
}
