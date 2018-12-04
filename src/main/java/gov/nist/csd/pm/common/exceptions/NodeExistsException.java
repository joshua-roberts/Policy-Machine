package gov.nist.csd.pm.common.exceptions;

import gov.nist.csd.pm.common.model.graph.nodes.Node;

public class NodeExistsException extends PMException {

    public NodeExistsException(Node node) {
        super(ErrorCodes.ERR_NODE_EXISTS, String.format("a node with the name %s with the type %s and properties %s already exists",
                node.getName(), node.getType(), node.getProperties()));
    }
}
