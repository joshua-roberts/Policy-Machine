package gov.nist.csd.pm.model.graph.relationships;

import gov.nist.csd.pm.model.exceptions.InvalidAssignmentException;
import gov.nist.csd.pm.model.graph.nodes.NodeType;

/**
 * This object represents an Assignment in a NGAC graph
 */
public class NGACAssignment extends NGACRelationship {

    public NGACAssignment(long childID, long parentID) {
        super(childID, parentID);
    }

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
