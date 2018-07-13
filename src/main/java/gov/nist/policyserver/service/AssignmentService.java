package gov.nist.policyserver.service;

import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;

import static gov.nist.policyserver.common.Constants.ALL_OPS;
import static gov.nist.policyserver.common.Constants.SUPER_KEYWORD;


public class AssignmentService extends Service{

    AssociationsService associationsService = new AssociationsService();
    NodeService nodeService = new NodeService();

    public boolean isAssigned(long childId, long parentId) throws NodeNotFoundException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        //check if the nodes exist
        Node child = getGraph().getNode(childId);
        if(child == null){
            throw new NodeNotFoundException(childId);
        }
        Node parent = getGraph().getNode(parentId);
        if(parent == null){
            throw new NodeNotFoundException(parentId);
        }

        return getGraph().isAssigned(child, parent);
    }

    public void createAssignment(long childId, long parentId) throws NodeNotFoundException, AssignmentExistsException, DatabaseException, InvalidAssignmentException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException, InvalidNodeTypeException, UnexpectedNumberOfNodesException, AssociationExistsException {
        //check if the nodes exist
        Node child = getGraph().getNode(childId);
        if(child == null){
            throw new NodeNotFoundException(childId);
        }
        Node parent = getGraph().getNode(parentId);
        if(parent == null){
            throw new NodeNotFoundException(parentId);
        }
        if (isAssigned(childId, parentId) ) {
            throw new AssignmentExistsException("Assignment exists between node " + childId + " and " + parentId);
        }

        //create assignment in database
        getDaoManager().getAssignmentsDAO().createAssignment(childId, parentId);

        //create assignment in nodes
        getGraph().createAssignment(child, parent);

        //if the parent is a PC and the child is an OA, create a Association for the super user on the child
        if (parent.getType().equals(NodeType.PC) && child.getType().equals(NodeType.OA)) {
            Node superUA = nodeService.getNode(SUPER_KEYWORD, SUPER_KEYWORD, NodeType.UA.toString(), null);

            //assign UA to PC
            if(!isAssigned(superUA.getId(), parentId)) {
                createAssignment(superUA.getId(), parentId);
            }

            //create Association
            associationsService.createAssociation(superUA.getId(), childId,
                    new HashSet<>(Collections.singleton(ALL_OPS)), true);
        }
    }

    public void deleteAssignment(long childId, long parentId) throws NodeNotFoundException, AssignmentDoesNotExistException, DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //check if the nodes exist
        Node child = getGraph().getNode(childId);
        if(child == null){
            throw new NodeNotFoundException(childId);
        }
        Node parent = getGraph().getNode(parentId);
        if(parent == null){
            throw new NodeNotFoundException(childId);
        }

        //check if the assignment exists
        if(!isAssigned(childId, parentId)){
            throw new AssignmentDoesNotExistException(childId, parentId);
        }

        //delete assignment in database
        getDaoManager().getAssignmentsDAO().deleteAssignment(childId, parentId);

        //delete assignment in nodes
        getGraph().deleteAssignment(child, parent);
    }

    public HashSet<Node> getAscendants(long nodeId) throws NodeNotFoundException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        Node node = getGraph().getNode(nodeId);
        if(node == null) {
            throw new NodeNotFoundException(nodeId);
        }

        return getGraph().getAscesndants(nodeId);
    }
}
