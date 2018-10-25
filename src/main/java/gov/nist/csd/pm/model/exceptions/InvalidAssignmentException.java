package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_INVALID_ASSIGNMENT;

public class InvalidAssignmentException extends PMException {
    public InvalidAssignmentException(Node child, Node parent){
        super(ERR_INVALID_ASSIGNMENT, "Cannot assign a node of type " + child.getType() + " to a node with type " + parent.getType());
    }

    public InvalidAssignmentException(NodeType childType, NodeType parentType){
        super(ERR_INVALID_ASSIGNMENT, "Cannot assign a node of type " + childType + " to a node with type " + parentType);
    }
}
