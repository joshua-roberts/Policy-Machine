package gov.nist.csd.pm.common.model.graph.relationships;

import gov.nist.csd.pm.common.exceptions.InvalidAssignmentException;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;

/**
 * This object represents an Assignment in a NGAC graph
 */
public class NGACAssignment extends NGACRelationship {

    public NGACAssignment(long childID, long parentID) {
        super(childID, parentID);
    }

    /**
     * Check if the assignment provided, is valid under NGAC.
     * @param childType The type of the child.
     * @param parentType The type of the parent.
     * @throws InvalidAssignmentException When the child type is not allowed to be assigned to the parent type.
     */
    public static void checkAssignment(NodeType childType, NodeType parentType) throws InvalidAssignmentException {
        switch (childType) {
            case PC:
                throw new InvalidAssignmentException(childType, parentType);
            case OA:
                switch (parentType) {
                    case O:
                    case UA:
                    case U:
                        throw new InvalidAssignmentException(childType, parentType);
                }
                break;
            case O:
                switch (parentType) {
                    case PC:
                    case UA:
                    case U:
                    case O:
                        throw new InvalidAssignmentException(childType, parentType);
                }
                break;
            case UA:
                switch (parentType) {
                    case OA:
                    case O:
                    case U:
                        throw new InvalidAssignmentException(childType, parentType);
                }
                break;
            case U:
                switch (parentType) {
                    case OA:
                    case PC:
                    case O:
                    case U:
                        throw new InvalidAssignmentException(childType, parentType);
                }
                break;
            default: throw new InvalidAssignmentException(childType, parentType);
        }
    }
}
