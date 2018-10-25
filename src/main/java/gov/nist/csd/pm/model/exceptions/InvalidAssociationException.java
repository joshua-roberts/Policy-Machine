package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;

import static gov.nist.csd.pm.model.exceptions.ErrorCodes.ERR_INVALID_ASSOCIATION;

public class InvalidAssociationException extends PMException {
    public InvalidAssociationException(Node ua, Node target){
        super(ERR_INVALID_ASSOCIATION, "Cannot associate a node of type " + ua.getType() + " to a node with type " + target.getType());
    }

    public InvalidAssociationException(NodeType uaType, NodeType targetType){
        super(ERR_INVALID_ASSOCIATION, "Cannot associate a node of type " + uaType + " to a node with type " + targetType);
    }
}
