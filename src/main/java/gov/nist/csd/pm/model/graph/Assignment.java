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
