package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.graph.Node;

public class NodeIDExistsException extends PmException {
    public NodeIDExistsException(long id, Node node) {
        super(ErrorCodes.ERR_NODE_ID_EXISTS, "A node already exists with ID " + id + ": name=" + node.getName() + ", type=" + node.getType());
    }
}
