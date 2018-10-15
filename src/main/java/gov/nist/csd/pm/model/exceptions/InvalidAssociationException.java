package gov.nist.csd.pm.model.exceptions;

import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.graph.NodeType;

import static gov.nist.csd.pm.pep.response.ApiResponseCodes.ERR_INVALID_ASSOCIATION;

public class InvalidAssociationException extends PmException {
    public InvalidAssociationException(Node ua, Node target){
        super(ERR_INVALID_ASSOCIATION, "Cannot associate a node of type " + ua.getType() + " to a node with type " + target.getType());
    }

    public InvalidAssociationException(NodeType uaType, NodeType targetType){
        super(ERR_INVALID_ASSOCIATION, "Cannot associate a node of type " + uaType + " to a node with type " + targetType);
    }
}
