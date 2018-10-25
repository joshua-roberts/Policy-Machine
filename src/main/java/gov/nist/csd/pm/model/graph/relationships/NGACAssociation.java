package gov.nist.csd.pm.model.graph.relationships;

import gov.nist.csd.pm.model.exceptions.InvalidAssociationException;
import gov.nist.csd.pm.model.graph.nodes.NodeType;

import java.util.Collection;
import java.util.HashSet;

/**
 * This object represents an Association in a NGAC graph
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
                        break;
                    default: throw new InvalidAssociationException(uaType, targetType);
                }
                break;
            default: throw new InvalidAssociationException(uaType, targetType);
        }
    }
}
