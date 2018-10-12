package gov.nist.csd.pm.model.graph;

import gov.nist.csd.pm.model.exceptions.*;;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface Graph {
    Node createUser(String name, Map<String, String> properties) throws DatabaseException, NodeExistsException;
    Node createUserAttribute(String name, Map<String, String> properties) throws DatabaseException, NodeExistsException;
    Node createObject(String name, Map<String, String> properties) throws DatabaseException, NodeExistsException;
    Node createObjectAttribute(String name, Map<String, String> properties) throws DatabaseException, NodeExistsException;
    Node createPolicyClass(String name, Map<String, String> properties) throws DatabaseException, NodeExistsException;

    void deleteNode(long id) throws NodeNotFoundException, DatabaseException;
    Node getNode(long id) throws NodeNotFoundException, DatabaseException;

    void createAssignment(long childID, long parentID) throws NodeNotFoundException, AssignmentExistsException, InvalidAssignmentException, DatabaseException;
    void deleteAssignment(long childID, long parentID) throws NodeNotFoundException, AssignmentDoesNotExistException, DatabaseException;
    HashSet<Node> getChildren(long id) throws NodeNotFoundException, DatabaseException;
    HashSet<Node> getParents(long id) throws NodeNotFoundException, DatabaseException;

    void createAssociation(long uaID, long targetID, String... operations) throws NodeNotFoundException, AssociationExistsException, InvalidAssociationException, DatabaseException;
    void updateAssociation(long uaID, long targetID, String... operations) throws NodeNotFoundException, AssociationDoesNotExistException, DatabaseException;
    void deleteAssociation(long uaID, long targetID) throws NodeNotFoundException, AssociationDoesNotExistException, DatabaseException;
    List<Association> getAssociations(long uaID) throws NodeNotFoundException, DatabaseException;
}
