package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.model.graph.nodes.Node;

public class NodeIDExistsException extends PMException {
    public NodeIDExistsException(long id, Node node) {
        super(ErrorCodes.ERR_NODE_ID_EXISTS, "A node already exists with ID " + id + ": name=" + node.getName() + ", type=" + node.getType());
    }
}
