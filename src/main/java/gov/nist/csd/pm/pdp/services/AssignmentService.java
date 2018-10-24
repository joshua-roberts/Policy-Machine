package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.demos.ndac.translator.exceptions.PMAccessDeniedException;
import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Assignment;
import gov.nist.csd.pm.model.graph.OldNode;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pdp.engine.PolicyDecider;
import gov.nist.csd.pm.model.graph.Search;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;

import static gov.nist.csd.pm.model.constants.Operations.*;
import static gov.nist.csd.pm.model.constants.Properties.ALL_OPS;
import static gov.nist.csd.pm.model.graph.nodes.NodeType.OA;
import static gov.nist.csd.pm.model.graph.nodes.NodeType.PC;


public class AssignmentService extends Service{

    /**
     * Create a new AssignmentService with the given sessionID and processID.
     */
    public AssignmentService(String sessionID, long processID) {
        super(sessionID, processID);
    }

    /**
     * Assign the node with childID
     * @param childID
     * @param parentID
     * @throws PMException
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void createAssignment(long childID, long parentID) throws PMException, IOException, SQLException, ClassNotFoundException {
        boolean exists = getMem().exists(childID);
        if(!exists){
            throw new NodeNotFoundException(childID);
        }
        exists = getMem().exists(parentID);
        if(!exists){
            throw new NodeNotFoundException(parentID);
        }
        if (isAssigned(childID, parentID) ) {
            throw new AssignmentExistsException("Assignment exists between node " + childID + " and " + parentID);
        }

        PolicyDecider decider = getPolicyDecider();
        if (!decider.canAssign(childID, parentID)) {
           throw new PMAccessDeniedException();
        }

        //get the nodes in order to check the assignment is allowed under NGAC
        Search search = getSearch();
        Node childNode = search.getNode(childID);
        Node parentNode = search.getNode(parentID);

        Assignment.checkAssignment(childNode.getType(), parentNode.getType());

        //create the assignment
        createAssignment(childNode, parentNode);
    }

    protected void createAssignment(Node child, Node parent) throws ClassNotFoundException, SQLException, PMException, IOException {
        //create assignment in database
        getDB().assign(child.getID(), parent.getID());

        //create assignment in nodes
        getMem().assign(child.getID(), parent.getID());

        //if the parent is a PC and the child is an OA, create a Association for the super user on the child
        if (parent.getType().equals(PC) && child.getType().equals(OA)) {
            Search search = getSearch();
            Node superUA = search.getNode(getSuperUAID());

            //assign UA to PC
            if(!isAssigned(superUA.getID(), parent.getID())) {
                createAssignment(superUA, parent);
            }

            //create Association
            AssociationsService associationsService = new AssociationsService();
            associationsService.createAssociation(superUA.getID(), child.getID(),
                    new HashSet<>(Collections.singleton(ALL_OPS)));
        }
    }

    public void deleteAssignment(String session, long process, long childID, long parentID) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, NodeNotFoundException, MissingPermissionException, AssignmentDoesNotExistException {
        OldNode user = getSessionUserID(session);

        OldNode child = getGraph().getNode(childID);
        if(child == null){
            throw new NodeNotFoundException(childID);
        }
        OldNode parent = getGraph().getNode(parentID);
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

    private boolean isAssigned(long childID, long parentID) throws PMException {
        // check that the childID is in the parent's children.
        return getMem().getChildren(parentID).contains(new Node().id(childID));
    }

    private void deleteAssignment(OldNode child, OldNode parent) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //delete assignment in database
        getDaoManager().getAssignmentsDAO().deleteAssignment(child.getID(), parent.getID());

        //delete assignment in nodes
        getGraph().deleteAssignment(child, parent);
    }

    public HashSet<OldNode> getAscendants(long nodeID) throws NodeNotFoundException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        OldNode node = getGraph().getNode(nodeID);
        if(node == null) {
            throw new NodeNotFoundException(nodeID);
        }

        return getGraph().getAscendants(nodeID);
    }
}
