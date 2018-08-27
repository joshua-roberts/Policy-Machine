package gov.nist.csd.pm.model.graph;


import gov.nist.csd.pm.model.exceptions.InvalidAssociationException;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

public class Association<V> extends Assignment  implements Serializable {
    private HashSet<String> operations;
    public Association(Node start, Node end, HashSet<String> ops) {
        super(start, end);
        this.operations = ops;
    }

    public HashSet<String> getOps(){
        return this.operations;
    }

    public void addOperation(String op) {
        operations.add(op);
    }

    public void addOperations(List<String> ops) {
        this.operations.addAll(ops);
    }

    public void removeOperations(HashSet<String> operations) {
        this.operations.removeAll(operations);
    }

    public void setOperations(HashSet<String> ops){
        this.operations = ops;
    }

    public void setAttributes(Node startNode, Node endNode) {
        this.child = startNode;
        this.parent = endNode;
    }

    public static void checkAssociation(NodeType uaType, NodeType targetType) throws InvalidAssociationException {
        switch (uaType) {
            case POLICY_CLASS:
            case OBJECT_ATTRIBUTE:
            case OBJECT:
            case USER:
                throw new InvalidAssociationException(uaType, targetType);
            case USER_ATTRIBUTE:
                switch (targetType) {
                    case OBJECT_ATTRIBUTE:
                        break;
                    default: throw new InvalidAssociationException(uaType, targetType);
                }
                break;
            default: throw new InvalidAssociationException(uaType, targetType);
        }
    }
}