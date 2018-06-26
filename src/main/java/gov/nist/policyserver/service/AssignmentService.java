package gov.nist.policyserver.service;

import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.model.graph.nodes.Node;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;


public class AssignmentService extends Service{

    public boolean isAssigned(long childId, long parentId) throws NodeNotFoundException, ClassNotFoundException, SQLException, DatabaseException, IOException {
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

    public void createAssignment(long childId, long parentId) throws NodeNotFoundException, AssignmentExistsException, DatabaseException, InvalidAssignmentException, SQLException, IOException, ClassNotFoundException {
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
    }

    public void deleteAssignment(long childId, long parentId) throws NodeNotFoundException, AssignmentDoesNotExistException, DatabaseException,SQLException, IOException, ClassNotFoundException {
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

    public HashSet<Node> getAscendants(long nodeId) throws NodeNotFoundException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        Node node = getGraph().getNode(nodeId);
        if(node == null) {
            throw new NodeNotFoundException(nodeId);
        }

        return getGraph().getAscesndants(nodeId);
    }
}
