package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Assignment;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.graph.NodeType;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;

import static gov.nist.csd.pm.model.Constants.*;
import static gov.nist.csd.pm.model.graph.NodeType.OA;
import static gov.nist.csd.pm.model.graph.NodeType.PC;


public class AssignmentService extends Service{

    private AssociationsService associationsService = new AssociationsService();
    private AnalyticsService  analyticsService  = new AnalyticsService();

    public boolean isAssigned(long childID, long parentID) throws NodeNotFoundException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        //check if the nodes exist
        Node child = getGraph().getNode(childID);
        if(child == null){
            throw new NodeNotFoundException(childID);
        }
        Node parent = getGraph().getNode(parentID);
        if(parent == null){
            throw new NodeNotFoundException(parentID);
        }

        return getGraph().isAssigned(child, parent);
    }

    public void createAssignment(long childID, long parentID, String session, long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, NodeNotFoundException, MissingPermissionException, AssignmentExistsException, InvalidAssignmentException, UnexpectedNumberOfNodesException, AssociationExistsException, InvalidNodeTypeException, PropertyNotFoundException, InvalidAssociationException {
        Node user = getSessionUser(session);

        Node child = getGraph().getNode(childID);
        if(child == null){
            throw new NodeNotFoundException(childID);
        }
        Node parent = getGraph().getNode(parentID);
        if(parent == null){
            throw new NodeNotFoundException(parentID);
        }

        //check assigning correct types
        Assignment.checkAssignment(child.getType(), parent.getType());

        //check if the nodes are already assigned
        if (isAssigned(childID, parentID) ) {
            throw new AssignmentExistsException("Assignment exists between node " + childID + " and " + parentID);
        }

        //check the user can assign the child
        canAssign(child, user, process);

        //check the user can assign to the parent
        canAssignTo(child, parent, user, process);

        //create the assignment
        createAssignment(child.getID(), parent.getID());
    }

    private void canAssign(Node child, Node user, long process) throws IOException, NodeNotFoundException, SQLException, InvalidProhibitionSubjectTypeException, DatabaseException, InvalidPropertyException, NoSubjectParameterException, ClassNotFoundException, ConfigurationException, MissingPermissionException {
        if(child.getType().equals(OA)) {
            analyticsService.checkPermissions(user, process, child.getID(), ASSIGN_OBJECT_ATTRIBUTE);
        } else if(child.getType().equals(NodeType.O)) {
            analyticsService.checkPermissions(user, process, child.getID(), ASSIGN_OBJECT);
        }
    }

    private void canAssignTo(Node child, Node parent, Node user, long process) throws IOException, NodeNotFoundException, SQLException, InvalidProhibitionSubjectTypeException, DatabaseException, InvalidPropertyException, NoSubjectParameterException, ClassNotFoundException, ConfigurationException, MissingPermissionException {
        if(child.getType().equals(OA)) {
            analyticsService.checkPermissions(user, process, parent.getID(), ASSIGN_OBJECT_ATTRIBUTE_TO);
        } else if(child.getType().equals(NodeType.O)) {
            analyticsService.checkPermissions(user, process, parent.getID(), ASSIGN_OBJECT_TO);
        }
    }

    public void createAssignment(long childID, long parentID) throws NodeNotFoundException, AssignmentExistsException, DatabaseException, InvalidAssignmentException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException, InvalidNodeTypeException, UnexpectedNumberOfNodesException, AssociationExistsException, PropertyNotFoundException, InvalidAssociationException {
        //check if the nodes exist
        Node child = getGraph().getNode(childID);
        if(child == null){
            throw new NodeNotFoundException(childID);
        }
        Node parent = getGraph().getNode(parentID);
        if(parent == null){
            throw new NodeNotFoundException(parentID);
        }
        if (isAssigned(childID, parentID) ) {
            throw new AssignmentExistsException("Assignment exists between node " + childID + " and " + parentID);
        }

        createAssignment(child, parent);
    }

    protected void createAssignment(Node child, Node parent) throws ClassNotFoundException, SQLException, InvalidPropertyException, IOException, DatabaseException, NodeNotFoundException, InvalidNodeTypeException, PropertyNotFoundException, AssociationExistsException, InvalidAssociationException {
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
            associationsService.createAssociation(superUA.getID(), child.getID(),
                    new HashSet<>(Collections.singleton(ALL_OPS)));
        }
    }

    private Node getSuperUA() throws ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException, InvalidNodeTypeException, PropertyNotFoundException, NodeNotFoundException {
        HashSet<Node> nodesOfType = getGraph().getNodesOfType(NodeType.UA);
        for(Node node : nodesOfType) {
            if(node.getName().equals(SUPER_KEYWORD)) {
                if(node.hasPropertyKey(NAMESPACE_PROPERTY) && node.getProperty(NAMESPACE_PROPERTY).equals(SUPER_KEYWORD)) {
                    return node;
                }
            }
        }

        throw new NodeNotFoundException(SUPER_KEYWORD);
    }

    public void deleteAssignment(String session, long process, long childID, long parentID) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, NodeNotFoundException, MissingPermissionException, AssignmentDoesNotExistException {
        Node user = getSessionUser(session);

        Node child = getGraph().getNode(childID);
        if(child == null){
            throw new NodeNotFoundException(childID);
        }
        Node parent = getGraph().getNode(parentID);
        if(parent == null){
            throw new NodeNotFoundException(childID);
        }

        //check if the assignment exists
        if(!isAssigned(childID, parentID)){
            throw new AssignmentDoesNotExistException(childID, parentID);
        }

        if(child.getType().equals(OA) || child.getType().equals(NodeType.O)) {
            //PERMISSION CHECK
            //check user can deassign the child node from the parent node
            //1. can assign TO parent node
            analyticsService.checkPermissions(user, process, parentID, DEASSIGN_FROM);

            //2. can deassign child
            analyticsService.checkPermissions(user, process, childID, DEASSIGN);
        }

        deleteAssignment(child, parent);
    }

    private void deleteAssignment(Node child, Node parent) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //delete assignment in database
        getDaoManager().getAssignmentsDAO().deleteAssignment(child.getID(), parent.getID());

        //delete assignment in nodes
        getGraph().deleteAssignment(child, parent);
    }

    public HashSet<Node> getAscendants(long nodeID) throws NodeNotFoundException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        Node node = getGraph().getNode(nodeID);
        if(node == null) {
            throw new NodeNotFoundException(nodeID);
        }

        return getGraph().getAscesndants(nodeID);
    }
}
