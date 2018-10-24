package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.graph.OldNode;
import gov.nist.csd.pm.model.exceptions.ErrorCodes;

public class NodeExistsException extends PMException {

    public NodeExistsException(OldNode node) {
        super(ErrorCodes.ERR_NODE_EXISTS, String.format("a node with the name %s with the type %s and properties %s already exists",
                node.getName(), node.getType(), node.getProperties()));
    }
}
