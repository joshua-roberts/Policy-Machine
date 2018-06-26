package gov.nist.policyserver.exceptions;

import gov.nist.policyserver.common.Constants;
import gov.nist.policyserver.model.graph.nodes.Node;

public class InvalidAssignmentException extends PmException {
    public InvalidAssignmentException(Node child, Node parent){
        super(Constants.ERR_INVALID_ASSIGNMENT, "Cannot assign a node of type " + child.getType() + " to a node with type " + parent.getType());
    }
}
