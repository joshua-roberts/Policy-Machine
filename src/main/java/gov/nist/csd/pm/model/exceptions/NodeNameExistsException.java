package gov.nist.csd.pm.model.exceptions;

public class NodeNameExistsException extends PmException {
    public NodeNameExistsException(String name) {
        super(ErrorCodes.ERR_NODE_NAME_EXISTS, String.format("A node with the name '%s' already exists", name));
    }
}
