package gov.nist.csd.pm.model.graph.relationships;

import gov.nist.csd.pm.model.exceptions.InvalidAssociationException;
import gov.nist.csd.pm.model.graph.nodes.NodeType;

import java.util.Collection;
import java.util.HashSet;

/**
 * This object represents an Association in a NGAC graph. An association is a relationship between two nodes,
 * similar to an assignment, except an Association has a set of operations included.
 */
public class NGACAssociation extends NGACRelationship {
    private HashSet<String> operations;

    public NGACAssociation(long uaID, long targetID, HashSet<String> operations) {
        super(uaID, targetID);
        this.operations = operations;
    }

    public HashSet<String> getOperations() {
        return operations;
    }

    public void setOperations(HashSet<String> operations) {
        this.operations = operations;
    }

    /**
     * Check if the provided types create a valid association.
     * @param uaType The type of the source node in the association. This should always be a user Attribute,
     *               so an InvalidAssociationException will be thrown if it's not.
     * @param targetType The type of the target node. This can be either an Object Attribute or a User Attribute.
     * @throws InvalidAssociationException When the provided types do not make a valid Association under NGAC
     */
    public static void checkAssociation(NodeType uaType, NodeType targetType) throws InvalidAssociationException {
        switch (uaType) {
            case PC:
            case OA:
            case O:
            case U:
                throw new InvalidAssociationException(uaType, targetType);
            case UA:
                switch (targetType) {
                    case OA:
                    case UA:
                        break;
                    default: throw new InvalidAssociationException(uaType, targetType);
                }
                break;
            default: throw new InvalidAssociationException(uaType, targetType);
        }
    }
}
