package gov.nist.policyserver.service;

import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;

import static gov.nist.policyserver.common.Constants.*;
import static gov.nist.policyserver.model.graph.nodes.NodeType.*;


public class AssignmentService extends Service{

    private AssociationsService associationsService = new AssociationsService();
    private AnalyticsService  analyticsService  = new AnalyticsService();

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

    public void createAssignment(String session, long process, long childId, long parentId, boolean checkAssign) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, NodeNotFoundException, MissingPermissionException, AssignmentExistsException, InvalidAssignmentException, UnexpectedNumberOfNodesException, AssociationExistsException, InvalidNodeTypeException, PropertyNotFoundException {
        Node user = getSessionUser(session);

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

        //check assigning correct types
        isValidAssignment(child.getType(), parent.getType());

        //if required, check the user can assign the child
        if(checkAssign) {
            canAssign(child, user, process);
        }

        //check the user can assign to the parent
        canAssignTo(child, parent, user, process);

        //create the assignment
        createAssignment(child.getId(), parent.getId());
    }

    public void canAssign(Node child, Node user, long process) throws IOException, NodeNotFoundException, SQLException, InvalidProhibitionSubjectTypeException, DatabaseException, InvalidPropertyException, NoSubjectParameterException, ClassNotFoundException, ConfigurationException, MissingPermissionException {
        if(child.getType().equals(NodeType.OA)) {
            analyticsService.checkPermissions(user, process, child.getId(), ASSIGN_OBJECT_ATTRIBUTE);
        } else if(child.getType().equals(NodeType.O)) {
            analyticsService.checkPermissions(user, process, child.getId(), ASSIGN_OBJECT);
        }
    }

    public void canAssignTo(Node child, Node parent, Node user, long process) throws IOException, NodeNotFoundException, SQLException, InvalidProhibitionSubjectTypeException, DatabaseException, InvalidPropertyException, NoSubjectParameterException, ClassNotFoundException, ConfigurationException, MissingPermissionException {
        if(child.getType().equals(NodeType.OA)) {
            analyticsService.checkPermissions(user, process, parent.getId(), ASSIGN_OBJECT_ATTRIBUTE_TO);
        } else if(child.getType().equals(NodeType.O)) {
            analyticsService.checkPermissions(user, process, parent.getId(), ASSIGN_OBJECT_TO);
        }
    }

    public void isValidAssignment(NodeType childType, NodeType parentType) throws InvalidAssignmentException {
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
            default: break;
        }
    }

    public void createAssignment(long childId, long parentId) throws NodeNotFoundException, AssignmentExistsException, DatabaseException, InvalidAssignmentException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException, InvalidNodeTypeException, UnexpectedNumberOfNodesException, AssociationExistsException, PropertyNotFoundException {
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

        createAssignment(child, parent);
    }

    public void createAssignment(Node child, Node parent) throws ClassNotFoundException, SQLException, InvalidPropertyException, IOException, DatabaseException, NodeNotFoundException, InvalidNodeTypeException, PropertyNotFoundException, AssociationExistsException {
        //create assignment in database
        getDaoManager().getAssignmentsDAO().createAssignment(child, parent);

        //create assignment in nodes
        getGraph().createAssignment(child, parent);

        //if the parent is a PC and the child is an OA, create a Association for the super user on the child
        if (parent.getType().equals(PC) && child.getType().equals(OA)) {
            Node superUA = getSuperUA();

            //assign UA to PC
            createAssignment(superUA, parent);

            //create Association
            associationsService.createAssociation(superUA.getId(), child.getId(),
                    new HashSet<>(Collections.singleton(ALL_OPS)), true);
        }
    }

    private Node getSuperUA() throws ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException, InvalidNodeTypeException, PropertyNotFoundException, NodeNotFoundException {
        HashSet<Node> nodesOfType = getGraph().getNodesOfType(NodeType.UA);
        for(Node node : nodesOfType) {
            if(node.getName().equals(SUPER_KEYWORD)) {
                if(node.hasProperty(NAMESPACE_PROPERTY) && node.getProperty(NAMESPACE_PROPERTY).getValue().equals(SUPER_KEYWORD)) {
                    return node;
                }
            }
        }

        throw new NodeNotFoundException(SUPER_KEYWORD);
    }

    public void deleteAssignment(String session, long process, long childId, long parentId) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, NodeNotFoundException, MissingPermissionException, AssignmentDoesNotExistException {
        Node user = getSessionUser(session);

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

        if(child.getType().equals(NodeType.OA) || child.getType().equals(NodeType.O)) {
            //PERMISSION CHECK
            //check user can deassign the child node from the parent node
            //1. can assign TO parent node
            analyticsService.checkPermissions(user, process, parentId, DEASSIGN_FROM);

            //2. can deassign child
            analyticsService.checkPermissions(user, process, childId, DEASSIGN);
        }

        deleteAssignment(child, parent);
    }

    public void deleteAssignment(Node child, Node parent) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //delete assignment in database
        getDaoManager().getAssignmentsDAO().deleteAssignment(child.getId(), parent.getId());

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
