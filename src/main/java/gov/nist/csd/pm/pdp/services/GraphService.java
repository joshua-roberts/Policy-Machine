package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.util.NodeUtils;
import gov.nist.csd.pm.decider.Decider;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.nodes.NodeType;
import gov.nist.csd.pm.graph.model.relationships.Assignment;
import gov.nist.csd.pm.graph.model.relationships.Association;
import gov.nist.csd.pm.pap.PAP;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static gov.nist.csd.pm.common.constants.Operations.*;
import static gov.nist.csd.pm.common.constants.Properties.*;
import static gov.nist.csd.pm.common.util.NodeUtils.generatePasswordHash;
import static gov.nist.csd.pm.graph.model.nodes.NodeType.OA;
import static gov.nist.csd.pm.graph.model.nodes.NodeType.PC;
import static gov.nist.csd.pm.pap.PAP.getPAP;

/**
 * GraphService provides methods to maintain an NGAC graph, while also ensuring any user interacting with the graph,
 * has the correct permissions to do so.
 */
public class GraphService extends Service {

    public GraphService(long userID, long processID) throws PMGraphException {
        super(userID, processID);
    }

    /**
     * Create a node and assign it to the node with the given ID. The name and type must not be null.
     * This method is needed because if a node is created without an initial assignment, it will be impossible
     * to assign the node in the future since no user will have permissions on a node not connected to the graph.
     * In this method we can check the user has the permission to assign to the given parent node and ignore if
     * the user can assign the newly created node.
     *
     * When creating a policy class, a parent node is not required.  The user must have the "create policy class" permission
     * on the super object.  By default the super user will always have this permission. A object attribute that represents the
     * policy class will be created and assigned to the super object attribute.  In the future, when any user attempts to perform
     * an action on the policy class they will need to have permission on this representative node.
     *
     * If the parent node is a policy class, check the permissions the current user has on the representative of the policy class.
     *
     * @param parentID the ID of the parent node if creating a non policy class node.
     * @param name the name of the node to create.
     * @param type the type of the node.
     * @param properties properties to add to the node.
     * @return the new node created with it's ID.
     * @throws IllegalArgumentException if the name is null or empty.
     * @throws IllegalArgumentException if the type is null.
     * @throws PMGraphException if a node already exists with the given name, type, and namespace property.
     * @throws PMAuthorizationException if the user does not have permission to create the node.
     * @throws PMConfigurationException if there is a configuration error in the PAP.
     */
    public Node createNode(long parentID, String name, NodeType type, Map<String, String> properties) throws PMException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("a node cannot have a null or empty name");
        } else if (type == null) {
            throw new IllegalArgumentException("a node cannot have a null type");
        }

        // instantiate the properties map if it's null
        // if this node is a user, hash the password if present in the properties
        if(properties == null) {
            properties = new HashMap<>();
        }
        if(properties.containsKey(PASSWORD_PROPERTY)) {
            try {
                properties.put(PASSWORD_PROPERTY, generatePasswordHash(properties.get(PASSWORD_PROPERTY)));
            }
            catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new PMGraphException(e.getMessage());
            }
        }

        checkNamespace(name, type, properties);

        long id = new Random().nextLong();
        // create the node
        if(type.equals(PC)) {
            return createPolicyClass(id, name, properties);
        } else {
            return createNonPolicyClass(parentID, id, name, type, properties);
        }
    }

    private void checkNamespace(String name, NodeType type, Map<String, String> properties) throws PMException {
        // check that the intended namespace does not already have the node name
        String namespace = properties.get(NAMESPACE_PROPERTY);
        if(namespace == null) {
            namespace = DEFAULT_NAMESPACE;
        }

        // search the graph for any node with the same name, type, and namespace
        Set<Node> search = getGraphPAP().search(name, type.toString(),
                NodeUtils.toProperties(NAMESPACE_PROPERTY, namespace));
        if(!search.isEmpty()) {
            throw new PMGraphException(String.format("a node with the name \"%s\" and type %s already exists in the namespace \"%s\"",
                    name, type, namespace));
        }
    }

    private Node createNonPolicyClass(long parentID, long id, String name, NodeType type, Map<String, String> properties) throws PMException {
        //check that the parent node exists
        if(!exists(parentID)) {
            throw new PMGraphException(String.format("the specified parent node with ID %d does not exist", parentID));
        }

        //get the parent node to make the assignment
        Node parentNodeCtx = getNode(parentID);

        // check if the parent is a PC and get the rep if it is
        long targetID = parentID;
        if(parentNodeCtx.getType().equals(PC)) {
            targetID = Long.parseLong(parentNodeCtx.getProperties().get(REP_PROPERTY));
        }

        // check that the user has the permission to assign to the parent node
        Decider decider = getDecider();
        if (!decider.hasPermissions(getUserID(), targetID, ASSIGN_TO)) {
            // if the user cannot assign to the parent node, delete the newly created node
            throw new PMAuthorizationException(String.format("unauthorized permission %s on node with ID %d", ASSIGN_TO, parentID));
        }

        //create the node
        Node node = getGraphPAP().createNode(id, name, type, properties);

        // assign the node to the specified parent node
        getGraphPAP().assign(id, parentID);

        return node;
    }

    private Node createPolicyClass(long id, String name, Map<String, String> properties) throws PMException {
        // check that the user can create a policy class
        Decider decider = getDecider();
        if (!decider.hasPermissions(getUserID(), getPAP().getSuperO().getID(), CREATE_POLICY_CLASS)) {
            throw new PMAuthorizationException("unauthorized permissions to create a policy class");
        }

        // create a node to represent the policy class in the super policy class
        // this way we can set permissions on this node to control who can (de)assign to the policy class node.
        HashMap<String, String> repProps = new HashMap<>();
        properties.forEach(repProps::putIfAbsent);
        
        long repID = new Random().nextLong();
        getGraphPAP().createNode(repID, name + " rep", NodeType.OA, repProps);

        // add the ID of the rep node to the properties of the policy class node
        properties.put(REP_PROPERTY, String.valueOf(repID));
        
        // create pc node
        Node pcNode = getGraphPAP().createNode(id, name, PC, properties);
        
        // assign the rep object in the super pc
        getGraphPAP().assign(repID, getPAP().getSuperOA().getID());

        return pcNode;
    }

    /**
     * Update the node in the database and in the in-memory graph.  If the name is null or empty it is ignored, likewise
     * for properties.
     *
     * @param id the ID of the node to update.
     * @param name the name to give the node.
     * @param properties the properties of the node.
     * @throws IllegalArgumentException if the given node id is 0.
     * @throws PMGraphException if the given node does not exist in the graph.
     * @throws PMConfigurationException if there is a configuration error in the PAP.
     * @throws PMAuthorizationException if the user is not authorized to update the node.
     */
    public void updateNode(long id, String name, Map<String, String> properties) throws PMException {
        if(id == 0) {
            throw new IllegalArgumentException("no ID was provided when updating the node");
        } else if (!exists(id)) {
            throw new PMGraphException(String.format("node with ID %d could not be found", id));
        }

        // check that the user can update the node
        Decider decider = getDecider();
        if(!decider.hasPermissions(getUserID(), id, UPDATE_NODE)) {
            throw new PMAuthorizationException(String.format("unauthorized permission %s on node with ID %d", UPDATE_NODE, id));
        }

        //update node in the PAP
        getGraphPAP().updateNode(id, name, properties);
    }

    /**
     * Delete the node with the given ID from the db and in-memory graphs.  First check that the current user
     * has the correct permissions to do so. Do this by checking that the user has the permission to deassign from each
     * of the node's parents, and that the user can delete the node.  If the node is a policy class or is assigned to a
     * policy class, check the permissions on the representative node.
     * @param nodeID the ID of the node to delete.
     * @throws PMGraphException if there is an error accessing the graph through the PAP.
     * @throws PMDBException if there is an error deleting the node in the database.
     * @throws PMConfigurationException if there is an error in the configuration of the PAP.
     * @throws PMAuthorizationException if the user is not authorized to delete the node.
     */
    public void deleteNode(long nodeID) throws PMException {
        Node node = getGraphPAP().getNode(nodeID);

        long repID = 0;
        long targetID = nodeID;
        if(node.getType().equals(PC)) {
            // if the node to delete is a PC, get the rep node
            targetID = Long.parseLong(node.getProperties().get(REP_PROPERTY));
            repID = targetID;
        }

        // check the user can deassign the node
        Decider decider = getDecider();
        if (!decider.hasPermissions(getUserID(), targetID, DEASSIGN)) {
            throw new PMAuthorizationException(String.format("unauthorized permissions on %s: %s", nodeID, DEASSIGN));
        }

        // check that the user can deassign from the node's parents
        Set<Long> parents = getGraphPAP().getParents(nodeID);
        for(long parentID : parents) {
            Node parent = getNode(parentID);
            // check the user can deassign from parent
            // if the parent is a policy class, get the rep oa
            if(parent.getType().equals(PC)) {
                targetID = Long.parseLong(parent.getProperties().get(REP_PROPERTY));
            } else {
                targetID = parent.getID();
            }
            if(!decider.hasPermissions(getUserID(), targetID, DEASSIGN_FROM)) {
                throw new PMAuthorizationException(String.format("unauthorized permissions on %s: %s", targetID, DEASSIGN_FROM));
            }
        }

        //if all checks pass, delete the node, thus deleting assignments
        //delete the node in the PAP
        getGraphPAP().deleteNode(nodeID);
        // if there is a rep ID, delete the rep
        if(repID != 0) {
            getGraphPAP().deleteNode(repID);
        }
    }

    /**
     * Check that a node with the given ID exists.  Just checking the in-memory graph is faster.
     * @param nodeID the ID of the node to check for.
     * @return true if a node with the given ID exists, false otherwise.
     * @throws PMGraphException if there is an error checking if the node exists in the graph through the PAP.
     * @throws PMDBException if the PAP accesses the database and an error occurs.
     * @throws PMConfigurationException if there is an error in the configuration of the PAP.
     */
    public boolean exists(long nodeID) throws PMException {
        return getGraphPAP().exists(nodeID);
    }

    /**
     * Retrieve the list of all nodes in the graph.  Go to the database to do this, since it is more likely to have
     * all of the node information.
     * @return the set of all nodes in the graph.
     * @throws PMGraphException if there is an error getting the nodes from the PAP.
     * @throws PMDBException if the PAP accesses the database and an error occurs.
     * @throws PMConfigurationException if there is an error in the configuration of the PAP.
     */
    public Set<Node> getNodes() throws PMException {
        Collection<Node> nodes = getGraphPAP().getNodes();
        List<Long> nodeIDs = new ArrayList<>();
        for(Node node : nodes) {
            nodeIDs.add(node.getID());
        }
        Collection<Long> filteredNodes = getDecider().filter(getUserID(), nodeIDs, ANY_OPERATIONS);
        nodes.removeIf(n -> !filteredNodes.contains(n.getID()));
        return new HashSet<>(nodes);
    }

    /**
     * Get the set of policy class IDs. This can be performed by the in-memory graph.
     * @return the set of IDs for the policy classes in the graph.
     * @throws PMGraphException if there is an error getting the policy classes from the PAP.
     * @throws PMDBException if the PAP accesses the database and an error occurs.
     * @throws PMConfigurationException if there is an error in the configuration of the PAP.
     */
    public Set<Long> getPolicies() throws PMException {
        return getGraphPAP().getPolicies();
    }

    /**
     * Get the children of the node from the graph.  Get the children from the database to ensure all node information
     * is present.  Before returning the set of nodes, filter out any nodes that the user has no permissions on.
     * @param nodeID the ID of the node to get the children of.
     * @return a set of Node objects, representing the children of the target node.
     * @throws PMGraphException if the target node does not exist.
     * @throws PMGraphException if there is an error getting the children from the PAP.
     * @throws PMDBException if the PAP accesses the database and an error occurs.
     * @throws PMConfigurationException if there is an error in the configuration of the PAP.

     */
    public Set<Node> getChildren(long nodeID) throws PMException {
        if(!exists(nodeID)) {
            throw new PMGraphException(String.format("node with ID %d could not be found", nodeID));
        }

        //filter any nodes that the user doesn't have any permissions on
        Collection<Long> children = getDecider().filter(getUserID(), getGraphPAP().getChildren(nodeID), ANY_OPERATIONS);
        Set<Node> retChildren = new HashSet<>();
        for(long childID : children) {
            retChildren.add(getNode(childID));
        }
        return retChildren;
    }

    /**
     * Get the parents of the node from the graph.  Get the parents from the database to ensure all node information
     * is present.  Before returning the set of nodes, filter out any nodes that the user has no permissions on.
     * @param nodeID the ID of the node to get the parents of.
     * @return a set of Node objects, representing the parents of the target node.
     * @throws PMGraphException if the target node does not exist.
     * @throws PMGraphException if there is an error getting the parents from the PAP.
     * @throws PMDBException if the PAP accesses the database and an error occurs.
     * @throws PMConfigurationException if there is an error in the configuration of the PAP.
     */
    public Set<Node> getParents(long nodeID) throws PMException {
        if(!exists(nodeID)) {
            throw new PMGraphException(String.format("node with ID %d could not be found", nodeID));
        }

        Collection<Long> parents = getDecider().filter(getUserID(), getGraphPAP().getParents(nodeID), ANY_OPERATIONS);
        Set<Node> retParents = new HashSet<>();
        for(long parentID : parents) {
            retParents.add(getNode(parentID));
        }
        return retParents;
    }

    /**
     * Create the assignment in both the db and in-memory graphs. First check that the user is allowed to assign the child,
     * and allowed to assign something to the parent. Both child and parent contexts must include the ID and type of the node.
     * @param childID the ID of the child node.
     * @param parentID the ID of the parent node.
     * @throws IllegalArgumentException if the child ID is 0.
     * @throws IllegalArgumentException if the parent ID is 0.
     * @throws PMGraphException if the child or parent node does not exist.
     * @throws PMGraphException if the assignment is invalid.
     * @throws PMDBException if the PAP accesses the database and there is an error.
     * @throws PMConfigurationException if there is an error in the configuration of the PAP.
     * @throws PMAuthorizationException if the current user does not have permission to create the assignment.
     */
    public void assign(long childID, long parentID) throws PMException {
        // check that the nodes are not null
        if(childID == 0) {
            throw new IllegalArgumentException("the child node ID cannot be 0 when creating an assignment");
        } else if(parentID == 0) {
            throw new IllegalArgumentException("the parent node ID cannot be 0 when creating an assignment");
        } else if(!exists(childID)) {
            throw new PMGraphException(String.format("child node with ID %d does not exist", childID));
        } else if(!exists(parentID)) {
            throw new PMGraphException(String.format("parent node with ID %d does not exist", parentID));
        }

        //check the user can assign the child
        Decider decider = getDecider();
        if(!decider.hasPermissions(getUserID(), childID, ASSIGN)) {
            throw new PMAuthorizationException(String.format("unauthorized permission %s on node with ID %d", ASSIGN, childID));
        }

        //check if the assignment is valid
        Node child = getNode(childID);
        Node parent = getNode(parentID);
        Assignment.checkAssignment(child.getType(), parent.getType());

        //check that the user can assign to the parent
        // if the parent is a PC, check for permissions on the rep node
        long targetID = parentID;
        if(parent.getType().equals(PC)) {
            // get the policy class node
            Node node = getGraphPAP().getNode(targetID);
            // get the rep property which is the ID of the rep node
            // set the target of the permission check to the rep node
            targetID = Long.parseLong(node.getProperties().get(REP_PROPERTY));
        }

        // check that the user can assign to the parent node
        if (!decider.hasPermissions(getUserID(), targetID, ASSIGN_TO)) {
            throw new PMAuthorizationException(String.format("unauthorized permission %s on node with ID %d", ASSIGN_TO, targetID));
        }

        // assign in the PAP
        getGraphPAP().assign(childID, parentID);
    }

    /**
     * Create the assignment in both the db and in-memory graphs. First check that the user is allowed to assign the child,
     * and allowed to assign something to the parent.
     * @param childID the ID of the child of the assignment to delete.
     * @param parentID the ID of the parent of the assignment to delete.
     * @throws IllegalArgumentException if the child ID is 0.
     * @throws IllegalArgumentException if the parent ID is 0.
     * @throws PMGraphException if the child or parent node does not exist.
     * @throws PMDBException if the PAP accesses the database and there is an error.
     * @throws PMConfigurationException if there is an error in the configuration of the PAP.
     * @throws PMAuthorizationException if the current user does not have permission to delete the assignment.
     */
    public void deassign(long childID, long parentID) throws PMException {
        // check that the parameters are correct
        if(childID == 0) {
            throw new IllegalArgumentException("the child node ID cannot be 0 when deassigning");
        } else if(parentID == 0) {
            throw new IllegalArgumentException("the parent node ID cannot be 0 when deassigning");
        } else if(!exists(childID)) {
            throw new PMGraphException(String.format("child node with ID %d could not be found when deassigning", childID));
        } else if(!exists(parentID)) {
            throw new PMGraphException(String.format("parent node with ID %d could not be found when deassigning", parentID));
        }

        Decider decider = getDecider();
        //check the user can deassign the child
        if(!decider.hasPermissions(getUserID(), childID, DEASSIGN)) {
            throw new PMAuthorizationException(String.format("unauthorized permissions on %s: %s", childID, DEASSIGN));
        }

        Node parent = getNode(parentID);

        //check that the user can deassign from the parent
        // if the parent is a PC, check for permissions on the rep node
        long targetID = parentID;
        if(parent.getType().equals(PC)) {
            // get the policy class node
            Node node = getGraphPAP().getNode(targetID);
            // get the rep property which is the ID of the rep node
            // set the target of the permission check to the rep node
            targetID = Long.parseLong(node.getProperties().get(REP_PROPERTY));
        }

        if (!decider.hasPermissions(getUserID(), targetID, DEASSIGN_FROM)) {
            throw new PMAuthorizationException(String.format("unauthorized permissions on %s: %s", targetID, DEASSIGN_FROM));
        }

        //delete assignment in PAP
        getGraphPAP().deassign(childID, parentID);
    }

    /**
     * Create an association between the user attribute and the target node with the given operations. First, check that
     * the user has the permissions to associate the user attribute and target nodes.  If an association already exists
     * between the two nodes than update the existing association with the provided operations (overwrite).
     * @param uaID the ID of the user attribute.
     * @param targetID the ID of the target node.
     * @param operations a Set of operations to add to the Association.
     * @throws IllegalArgumentException if the user attribute ID is 0.
     * @throws IllegalArgumentException if the target node ID is 0.
     * @throws PMGraphException if the user attribute node does not exist.
     * @throws PMGraphException if the target node does not exist.
     * @throws PMGraphException if the association is invalid.
     * @throws PMDBException if the PAP accesses the database and there is an error.
     * @throws PMConfigurationException if there is an error in the configuration of the PAP.
     * @throws PMAuthorizationException if the current user does not have permission to create the association.
     */
    public void associate(long uaID, long targetID, Set<String> operations) throws PMException {
        if(uaID == 0) {
            throw new IllegalArgumentException("the user attribute ID cannot be 0 when creating an association");
        } else if(targetID == 0) {
            throw new IllegalArgumentException("the target node ID cannot be 0 when creating an association");
        } else if(!exists(uaID)) {
            throw new PMGraphException(String.format("node with ID %d could not be found when creating an association", uaID));
        } else if(!exists(targetID)) {
            throw new PMGraphException(String.format("node with ID %d could not be found when creating an association", targetID));
        }

        Node sourceNode = getNode(uaID);
        Node targetNode = getNode(targetID);

        Association.checkAssociation(sourceNode.getType(), targetNode.getType());

        //check the user can associate the source and target nodes
        Decider decider = getDecider();
        if(!decider.hasPermissions(getUserID(), uaID, ASSOCIATE)) {
            throw new PMAuthorizationException(String.format("unauthorized permissions on %s: %s", sourceNode.getName(), ASSOCIATE));
        }
        if (!decider.hasPermissions(getUserID(), targetID, ASSOCIATE)) {
            throw new PMAuthorizationException(String.format("unauthorized permissions on %s: %s", targetNode.getName(), ASSOCIATE));
        }

        //create association in PAP
        getGraphPAP().associate(uaID, targetID, operations);
    }

    /**
     * Delete the association between the user attribute and the target node.  First, check that the user has the
     * permission to delete the association.
     * @param uaID The ID of the user attribute.
     * @param targetID The ID of the target node.
     * @throws IllegalArgumentException If the user attribute ID is 0.
     * @throws IllegalArgumentException If the target node ID is 0.
     * @throws PMGraphException If the user attribute node does not exist.
     * @throws PMGraphException If the target node does not exist.
     * @throws PMDBException if the PAP accesses the database and there is an error.
     * @throws PMConfigurationException if there is an error in the configuration of the PAP.
     * @throws PMAuthorizationException If the current user does not have permission to delete the association.
     */
    public void dissociate(long uaID, long targetID) throws PMException {
        if(uaID == 0) {
            throw new IllegalArgumentException("the user attribute ID cannot be 0 when creating an association");
        } else if(targetID == 0) {
            throw new IllegalArgumentException("the target node ID cannot be 0 when creating an association");
        } else if(!exists(uaID)) {
            throw new PMGraphException(String.format("node with ID %d could not be found when creating an association", uaID));
        } else if(!exists(targetID)) {
            throw new PMGraphException(String.format("node with ID %d could not be found when creating an association", targetID));
        }

        //check the user can associate the source and target nodes
        Decider decider = getDecider();
        if(!decider.hasPermissions(getUserID(), uaID, DISASSOCIATE)) {
            throw new PMAuthorizationException(String.format("unauthorized permissions on %s: %s", uaID, DISASSOCIATE));
        }
        if (!decider.hasPermissions(getUserID(), targetID, DISASSOCIATE)) {
            throw new PMAuthorizationException(String.format("unauthorized permissions on %s: %s", targetID, DISASSOCIATE));
        }

        //create association in PAP
        getGraphPAP().dissociate(uaID, targetID);
    }

    /**
     * Get the associations the given node is the source node of. First, check if the user is allowed to retrieve this
     * information.
     * @param sourceID The ID of the source node.
     * @return a map of the target ID and operations for each association the given node is the source of.
     * @throws PMGraphException If the given node does not exist.
     * @throws PMDBException if the PAP accesses the database and there is an error.
     * @throws PMConfigurationException if there is an error in the configuration of the PAP.
     * @throws PMAuthorizationException If the current user does not have permission to get hte node's associations.
     */
    public Map<Long, Set<String>> getSourceAssociations(long sourceID) throws PMException {
        if(!exists(sourceID)) {
            throw new PMGraphException(String.format("node with ID %d could not be found", sourceID));
        }

        //check the user can get the associations of the source node
        Decider decider = getDecider();
        if(!decider.hasPermissions(getUserID(), sourceID, GET_ASSOCIATIONS)){
            throw new PMAuthorizationException(String.format("unauthorized permissions on %s: %s", sourceID, GET_ASSOCIATIONS));
        }

        return getGraphPAP().getSourceAssociations(sourceID);
    }

    /**
     * Get the associations the given node is the target node of. First, check if the user is allowed to retrieve this
     * information.
     * @param targetID The ID of the source node.
     * @return a map of the source ID and operations for each association the given node is the target of.
     * @throws PMGraphException If the given node does not exist.
     * @throws PMDBException if the PAP accesses the database and there is an error.
     * @throws PMConfigurationException if there is an error in the configuration of the PAP.
     * @throws PMAuthorizationException If the current user does not have permission to get hte node's associations.
     */
    public Map<Long, Set<String>> getTargetAssociations(long targetID) throws PMException {
        if(!exists(targetID)) {
            throw new PMGraphException(String.format("node with ID %d could not be found", targetID));
        }

        //check the user can get the associations of the source node
        Decider decider = getDecider();
        if(!decider.hasPermissions(getUserID(), targetID, GET_ASSOCIATIONS)){
            throw new PMAuthorizationException(String.format("unauthorized permissions on %s: %s", targetID, GET_ASSOCIATIONS));
        }

        return getGraphPAP().getTargetAssociations(targetID);
    }

    /**
     * Search the NGAC graph for nodes that match the given parameters.  The given search parameters are provided in the
     * URI as query parameters.  The parameters name and type are extracted from the URI and passed as parameters to the
     * search function.  Any other query parameters found in the URI will be added to the search criteria as node properties.
     * A node must match all non null parameters to be returned in the search.
     *
     * @param name The name of the nodes to search for.
     * @param type The type of the nodes to search for.
     * @param properties The properties of the nodes to search for.
     * @return a Response with the nodes that match the given search criteria.
     * @throws PMGraphException If the PAP encounters an error with the graph.
     * @throws PMDBException if the PAP accesses the database and there is an error.
     * @throws PMConfigurationException if there is an error in the configuration of the PAP.
     * @throws PMAuthorizationException If the current user does not have permission to get hte node's associations.
     */
    public Set<Node> search(String name, String type, Map<String, String> properties) throws PMException {
        // user the PAP searcher to search for the intended nodes
        Set<Node> nodes = getGraphPAP().search(name, type, properties);
        nodes.removeIf(x -> {
            try {
                return !getDecider().hasPermissions(getUserID(), x.getID(), ANY_OPERATIONS);
            }
            catch (PMException e) {
                return true;
            }
        });
        return nodes;
    }

    /**
     * Retrieve the node from the graph with the given ID.
     *
     * @param id the ID of the node to get.
     * @return the Node retrieved from the graph with the given ID.
     * @throws PMGraphException If the node does not exist in the graph.
     * @throws PMGraphException If the node is a policy class that doesn't have a rep node.
     * @throws PMDBException if the PAP accesses the database and there is an error.
     * @throws PMAuthorizationException if the current user is not authorized to access this node.
     * @throws PMDBException if the PAP accesses the database and there is an error.
     */
    public Node getNode(long id) throws PMException {
        if(!exists(id)) {
            throw new PMGraphException(String.format("node with ID %d could not be found", id));
        }

        Node node = getGraphPAP().getNode(id);
        if(node.getType().equals(PC)) {
            id = Long.parseLong(node.getProperties().get(REP_PROPERTY));
            if(id == 0) {
                throw new PMGraphException(String.format("the policy class with ID %d does not have a rep node", node.getID()));
            }
        }

        Decider decider = getDecider();
        if(!decider.hasPermissions(getUserID(), id, ANY_OPERATIONS)) {
            throw new PMAuthorizationException(String.format("unauthorized permissions on %s: %s", id, ANY_OPERATIONS));
        }

        return node;
    }
}
