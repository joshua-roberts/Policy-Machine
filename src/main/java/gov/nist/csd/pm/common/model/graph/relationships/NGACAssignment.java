package gov.nist.csd.pm.common.model.graph.relationships;

import gov.nist.csd.pm.common.exceptions.Errors;
import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;

import java.util.HashMap;

import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.*;

/**
 * This object represents an Assignment in a NGAC graph
 */
public class NGACAssignment extends NGACRelationship {

    public NGACAssignment(long childID, long parentID) {
        super(childID, parentID);
    }


    private static HashMap<NodeType, NodeType[]> validAssignments = new HashMap<>();
    {
        validAssignments.put(PC, new NodeType[]{});
        validAssignments.put(OA, new NodeType[]{PC, OA});
        validAssignments.put(O, new NodeType[]{OA});
        validAssignments.put(UA, new NodeType[]{UA, PC});
        validAssignments.put(U, new NodeType[]{UA});
    }
    /**
     * Check if the assignment provided, is valid under NGAC.
     * @param childType The type of the child.
     * @param parentType The type of the parent.
     * @throws PMException When the child type is not allowed to be assigned to the parent type.
     */
    public static void checkAssignment(NodeType childType, NodeType parentType) throws PMException {
        NodeType[] check = validAssignments.get(childType);
        for(NodeType nt : check) {
            if(nt.equals(parentType)) {
                return;
            }
        }

        throw new PMException(Errors.ERR_INVALID_ASSIGNMENT,
                String.format("cannot assign a node of type %s to a node of type %s", childType, parentType));
    }
}
