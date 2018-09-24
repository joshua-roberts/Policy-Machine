package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.graph.Node;

public class NodeExistsException extends PmException{

    public NodeExistsException(Node node) {
        super(ErrorCodes.ERR_NODE_EXISTS, String.format("a node with the name %s with the type %s and properties %s already exists",
                node.getName(), node.getType(), node.getProperties()));
    }
}
