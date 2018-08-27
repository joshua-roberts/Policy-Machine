package gov.nist.csd.pm.model.graph;

import gov.nist.csd.pm.model.exceptions.InvalidAssignmentException;
import org.jgrapht.graph.DefaultEdge;

import java.io.Serializable;

public class Assignment<V> extends DefaultEdge implements Serializable {
    Node child;
    Node parent;

    public Assignment(Node child, Node parent){
        this.child = child;
        this.parent = parent;
    }

    public Node getChild() {
        return child;
    }

    public Node getParent() {
        return parent;
    }

    public boolean equals(Object o){
        if(o instanceof Assignment){
            Assignment e = (Assignment)o;
            return child == e.getChild() && parent == e.getParent();
        }
        return false;
    }

    public static void checkAssignment(NodeType childType, NodeType parentType) throws InvalidAssignmentException {
        switch (childType) {
            case POLICY_CLASS:
                throw new InvalidAssignmentException(childType, parentType);
            case OBJECT_ATTRIBUTE:
                switch (parentType) {
                    case OBJECT:
                    case USER_ATTRIBUTE:
                    case USER:
                        throw new InvalidAssignmentException(childType, parentType);
                }
                break;
            case OBJECT:
                switch (parentType) {
                    case POLICY_CLASS:
                    case USER_ATTRIBUTE:
                    case USER:
                        throw new InvalidAssignmentException(childType, parentType);
                }
                break;
            case USER_ATTRIBUTE:
                switch (parentType) {
                    case OBJECT_ATTRIBUTE:
                    case OBJECT:
                    case USER:
                        throw new InvalidAssignmentException(childType, parentType);
                }
                break;
            case USER:
                switch (parentType) {
                    case OBJECT_ATTRIBUTE:
                    case POLICY_CLASS:
                    case OBJECT:
                    case USER:
                        throw new InvalidAssignmentException(childType, parentType);
                }
                break;
            default: throw new InvalidAssignmentException(childType, parentType);
        }
    }
}
