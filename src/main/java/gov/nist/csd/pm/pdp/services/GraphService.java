package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Graph;
import gov.nist.csd.pm.model.graph.Search;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.model.graph.relationships.NGACAssignment;
import gov.nist.csd.pm.model.graph.relationships.NGACAssociation;
import gov.nist.csd.pm.pdp.engine.MemPolicyDecider;
import gov.nist.csd.pm.pdp.engine.PolicyDecider;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static gov.nist.csd.pm.model.constants.Operations.*;
import static gov.nist.csd.pm.model.constants.Properties.PASSWORD_PROPERTY;

/**
 * GraphService provides methods to maintain an NGAC graph, while also ensuring any user interacting with the graph,
 * has the correct permissions to do so.
 */
public class GraphService extends Service implements Graph, Search {

    public GraphService(String sessionID, long processID) {
        super(sessionID, processID);
    }

    /**
     * Create a node and assign it to the node with the given ID. The name and type must not be null.
     * This method is needed because if a node is created without an initial assignment.  It will be impossible
     * to assign the node in the future since no user will have permissions on a node not connected to the graph.
     * In this method we can check the user has the permission to assign to the given parent node and ignore if
     * the user can assign the newly created node.
     *
     * @param parentID The ID of the node to assign the new node to.
     * @param ctx The Node to create.
     * @return The new node created with it's ID.
     */
    public Node createNode(long parentID, Node ctx) throws NullNameException, NullTypeException, DatabaseException, LoadConfigException, LoaderException, NodeNotFoundException, HashingUserPasswordException, NullNodeException, NoIDException, MissingPermissionException, InvalidNodeTypeException, SessionDoesNotExistException, InvalidAssignmentException, InvalidProhibitionSubjectTypeException {
        if(ctx == null) {
            throw new NullNodeException();
        }

        String name = ctx.getName();
        NodeType type = ctx.getType();
        HashMap<String, String> properties = ctx.getProperties();

        //check that the node parameters are not null
        if(name == null) {
            throw new NullNameException();
        }else if (type == null) {
            throw new NullTypeException();
        } else if (properties == null) {
            // if the properties are null, instantiate
            properties = new HashMap<>();
        }

        //check that the parent node exists
        if(!exists(parentID)) {
            throw new NodeNotFoundException(parentID);
        }

        //if this node is a user, hash the password if present in the properties
        if(properties.containsKey(PASSWORD_PROPERTY)) {
            try {
                properties.put(PASSWORD_PROPERTY, generatePasswordHash(properties.get(PASSWORD_PROPERTY)));
            }
            catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new HashingUserPasswordException();
            }
        }

        //create the node
        long id = createNode(ctx);
        Node childNode = getNode(id);

        //get the parent node to make the assignment
        Node parentNode = getNode(parentID);

        //assign the new node to the parent
        assign(childNode.getID(), childNode.getType(), parentNode.getID(), parentNode.getType());

        return childNode;
    }

    /**
     * Create a new node in the database and in memory graphs.  No need to check permissions here, assigning the node is
     * where we check the user has the correct permissions.
     *
     * @param node The context of the node to create.  This includes the id, name, type, and properties.
     * @return The Node representing the node that was just created.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws LoadConfigException If the server cannot load the database configuration.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws NullNodeException If the provided Node context is null.
     * @throws NoIDException If the given node context does not already have the ID.
     * @throws NullTypeException If the given node context does not have a type.
     * @throws NullNameException If the given node context does not have a name.
     */
    @Override
    public long createNode(Node node) throws LoadConfigException, DatabaseException, LoaderException, NullNodeException, NoIDException, NullTypeException, NullNameException, InvalidProhibitionSubjectTypeException {
        if(node == null) {
            throw new NullNodeException();
        }

        //create node in database
        long id = getGraphDB().createNode(node);
        //add node to in-memory graph with the ID
        getGraphMem().createNode(node.id(id));

        return id;
    }

    /**
     * Update the node in the database and in the in-memory graph
     * @param node The context of the node to update. This includes the id, name, type, and properties.
     * @throws NullNodeException If either of the provided Node contexts are null.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws LoadConfigException If the server cannot load the database configuration.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws NullNodeException If the provided Node context is null.
     */
    @Override
    public void updateNode(Node node) throws LoadConfigException, DatabaseException, LoaderException, NullNodeException, NoIDException, NodeNotFoundException, InvalidProhibitionSubjectTypeException {
        if(node == null) {
            throw new NullNodeException();
        } else if (!exists(node.getID())) {
            throw new NodeNotFoundException(node.getID());
        }

        //update node in database
        getGraphDB().updateNode(node);
        //update node in in-memory graph
        getGraphMem().updateNode(node);
    }

    /**
     * Delete the node with the given ID from the db and in-memory graphs.  First check that the current user
     * has the correct permissions to do so. Do this by checking that the user has the permission to deassign from each
     * of the node's parents, and that the user can delete the object
     * @param nodeID the ID of the node to delete.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws SessionDoesNotExistException If the current session ID does not exist.
     * @throws LoadConfigException If the server cannot load the database configuration.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws MissingPermissionException If the current user does not the correct permissions.
     */
    @Override
    public void deleteNode(long nodeID) throws LoaderException, SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException, NodeNotFoundException, InvalidProhibitionSubjectTypeException {
        //check that the user can delete the node
        PolicyDecider decider = new MemPolicyDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        if (!decider.hasPermissions(getSessionUserID(), getProcessID(), nodeID, DELETE_NODE)) {
            throw new MissingPermissionException(nodeID, DELETE_NODE);
        }
        //check the user can deassign the node
        if (!decider.hasPermissions(getSessionUserID(), getProcessID(), nodeID, DEASSIGN)) {
            throw new MissingPermissionException(nodeID, DEASSIGN);
        }

        //check that the user can deassign from the node's parents
        HashSet<Node> parents = getGraphMem().getParents(nodeID);
        for(Node parent : parents) {
            //check the user can deassign from parent
            if(!decider.hasPermissions(getSessionUserID(), getProcessID(), parent.getID(), DEASSIGN_FROM)) {
                throw new MissingPermissionException(parent.getID(), DEASSIGN_FROM);
            }
        }

        //if all checks pass, delete the node, thus deleting assignments
        //delete the node in db
        getGraphDB().deleteNode(nodeID);
        //delete the node in-memory
        getGraphMem().deleteNode(nodeID);
    }

    /**
     * Check that a node with the given ID exists.  Just checking the in-memory graph is faster.
     * @param nodeID the ID of the node to check for.
     * @return True if a node with the given ID exists, false otherwise.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws LoadConfigException If the server cannot load the database configuration.
     */
    @Override
    public boolean exists(long nodeID) throws LoadConfigException, DatabaseException, LoaderException, InvalidProhibitionSubjectTypeException {
        return getGraphMem().exists(nodeID);
    }

    /**
     * Retrieve the list of all nodes in the graph.  Go to the database to do this, since it is more likely to have
     * all of the node information.
     * @return The set of all nodes in the graph.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws LoadConfigException If the server cannot load the database configuration.
     */
    @Override
    public HashSet<Node> getNodes() throws LoadConfigException, DatabaseException, LoaderException, InvalidProhibitionSubjectTypeException {
        return getGraphDB().getNodes();
    }

    /**
     * Get the set of policy class IDs. This can be performed by the in-memory graph.
     * @return The set of IDs for the policy classes in the graph.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws LoadConfigException If the server cannot load the database configuration.
     */
    @Override
    public HashSet<Long> getPolicies() throws LoadConfigException, DatabaseException, LoaderException, InvalidProhibitionSubjectTypeException {
        return getGraphMem().getPolicies();
    }

    /**
     * Get the children of the node from the graph.  Get the children from the database to ensure all node information
     * is present.  Before returning the set of nodes, filter out any nodes that the user has no permissions on.
     * @param nodeID The ID of the node to get the children of.
     * @return The children of the given Node that the user has any permissions on.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws LoadConfigException If the server cannot load the database configuration.
     * @throws SessionDoesNotExistException If the current session ID does not exist.

     */
    @Override
    public HashSet<Node> getChildren(long nodeID) throws LoaderException, SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException, NodeNotFoundException, InvalidProhibitionSubjectTypeException {
        if(!exists(nodeID)) {
            throw new NodeNotFoundException(nodeID);
        }

        //get the children from the db
        HashSet<Node> children = getGraphDB().getChildren(nodeID);
        //filter any nodes that the user doesn't have any permissions on
        return new MemPolicyDecider(getGraphMem(), getProhibitionsMem().getProhibitions()).filter(getSessionUserID(), getProcessID(), children, ANY_OPERATIONS);
    }

    /**
     * Get the parents of the node from the graph.  Get the parents from the database to ensure all node information
     * is present.  Before returning the set of nodes, filter out any nodes that the user has no permissions on.
     * @param nodeID The ID of the node to get the parents of.
     * @return The parents of the given Node that the user has any permissions on.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws LoadConfigException If the server cannot load the database configuration.
     * @throws SessionDoesNotExistException If the current session ID does not exist.
     */
    @Override
    public HashSet<Node> getParents(long nodeID) throws LoadConfigException, DatabaseException, LoaderException, SessionDoesNotExistException, MissingPermissionException, NodeNotFoundException, InvalidProhibitionSubjectTypeException {
        if(!exists(nodeID)) {
            throw new NodeNotFoundException(nodeID);
        }

        //get the children from the db
        HashSet<Node> children = getGraphDB().getParents(nodeID);
        //filter any nodes that the user doesn't have any permissions on
        return new MemPolicyDecider(getGraphMem(), getProhibitionsMem().getProhibitions()).filter(getSessionUserID(), getProcessID(), children, ANY_OPERATIONS);
    }

    /**
     * Create the assignment in both the db and in-memory graphs. First check that the user is allowed to assign the child,
     * and allowed to assign something to the parent.
     * @param childID The ID of the child node.
     * @param childType The type of the child node.
     * @param parentID The the ID of the parent node.
     * @param parentType The type of the parent node.
     * @throws NullNodeException If either of the provided Node contexts are null.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws SessionDoesNotExistException If the current session ID does not exist.
     * @throws LoadConfigException If the server cannot load the database configuration.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws MissingPermissionException If the current user does not the correct permissions.
     * @throws NullTypeException If either of the provided contexts have a null type.
     */
    @Override
    public void assign(long childID, NodeType childType, long parentID, NodeType parentType) throws NullNodeException, LoaderException, SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException, NullTypeException, NodeNotFoundException, InvalidAssignmentException, InvalidProhibitionSubjectTypeException {
        //check that the nodes exist
        if(!exists(childID)) {
            throw new NodeNotFoundException(childID);
        }
        if(!exists(parentID)) {
            throw new NodeNotFoundException(parentID);
        }

        //check if the assignment is valid
        NGACAssignment.checkAssignment(childType, parentType);

        //check the user can assign the child
        PolicyDecider decider = new MemPolicyDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(), childID, ASSIGN)) {
            throw new MissingPermissionException(childID, ASSIGN);
        }
        //check that the user can assign to the parent
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(), parentID, ASSIGN_TO)) {
            throw new MissingPermissionException(parentID, ASSIGN_TO);
        }

        //create assignment in db
        getGraphDB().assign(childID, childType, parentID, parentType);
        //create assignment in-memory
        getGraphMem().assign(childID, childType, parentID, parentType);
    }

    /**
     * Create the assignment in both the db and in-memory graphs. First check that the user is allowed to assign the child,
     * and allowed to assign something to the parent.
     * @param childID The ID the child node.
     * @param childType The type of the child node.
     * @param parentID The ID of the parent node.
     * @param parentType The type of the parent node.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws SessionDoesNotExistException If the current session ID does not exist.
     * @throws LoadConfigException If the server cannot load the database configuration.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws MissingPermissionException If the current user does not the correct permissions.
     * @throws NullTypeException If either of the provided contexts have a null type.
     */
    @Override
    public void deassign(long childID, NodeType childType, long parentID, NodeType parentType) throws InvalidProhibitionSubjectTypeException, LoaderException, LoadConfigException, DatabaseException, NodeNotFoundException, SessionDoesNotExistException, MissingPermissionException, NullNodeException, NullTypeException {

        //check nodes exist
        if(!exists(childID)) {
            throw new NodeNotFoundException(childID);
        }
        if(!exists(parentID)) {
            throw new NodeNotFoundException(parentID);
        }

        PolicyDecider decider = new MemPolicyDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        //check the user can deassign the child
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(), childID, DEASSIGN)) {
            throw new MissingPermissionException(childID, DEASSIGN);
        }
        //check that the user can deassign from the parent
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(),parentID, DEASSIGN_FROM)) {
            throw new MissingPermissionException(parentID, DEASSIGN);
        }

        //delete assignment in db
        getGraphDB().deassign(childID, childType, parentID, parentType);
        //delete assignment in-memory
        getGraphMem().deassign(childID, childType, parentID, parentType);
    }

    /**
     * Create an association between the User Attribute and the target node with the given operations. First, check that
     * the user has the permissions to associate the user attribute and target nodes.
     * @param uaID The ID of the User Attribute.
     * @param targetID The ID of the target node.
     * @param operations A Set of operations to add to the Association.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws SessionDoesNotExistException If the current session ID does not exist.
     * @throws LoadConfigException If the server cannot load the database configuration.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws MissingPermissionException If the current user does not the correct permissions.
     */
    @Override
    public void associate(long uaID, long targetID, NodeType targetType, HashSet<String> operations) throws DatabaseException, LoadConfigException, LoaderException, MissingPermissionException, SessionDoesNotExistException, NodeNotFoundException, InvalidNodeTypeException, InvalidAssociationException, InvalidProhibitionSubjectTypeException {
        //check that the nodes exist and the operations are not null
        if(!exists(uaID)) {
            throw new NodeNotFoundException(uaID);
        }
        if(!exists(targetID)) {
            throw new NodeNotFoundException(targetID);
        }
        if(operations == null) {
            operations = new HashSet<>();
        }

        Node sourceNode = getNode(uaID);
        Node targetNode = getNode(targetID);

        NGACAssociation.checkAssociation(sourceNode.getType(), targetNode.getType());

        //check the user can associate the source and target nodes
        PolicyDecider decider = new MemPolicyDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(), uaID, ASSOCIATE)) {
            throw new MissingPermissionException(uaID, ASSOCIATE);
        }
        if (!decider.hasPermissions(getSessionUserID(), getProcessID(), targetID, ASSOCIATE)) {
            throw new MissingPermissionException(targetID, ASSOCIATE);
        }

        //create association in db
        getGraphDB().associate(uaID, targetID, targetType, operations);
        //create association in-memory
        getGraphMem().associate(uaID, targetID, targetType, operations);
    }

    /**
     * Delete the association between the user attribute and the target node.  First, check that the user has the
     * permission to delete the association.
     * @param uaID The ID of the User Attribute.
     * @param targetID The ID of the target node.
     */
    @Override
    public void dissociate(long uaID, long targetID, NodeType targetType) throws DatabaseException, LoaderException, SessionDoesNotExistException, LoadConfigException, MissingPermissionException, NodeNotFoundException, InvalidProhibitionSubjectTypeException {
        //check the user can associate the source and target nodes
        PolicyDecider decider = new MemPolicyDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(), uaID, DISASSOCIATE)) {
            throw new MissingPermissionException(uaID, DISASSOCIATE);
        }
        if (!decider.hasPermissions(getSessionUserID(), getProcessID(), targetID, DISASSOCIATE)) {
            throw new MissingPermissionException(targetID, DISASSOCIATE);
        }

        //create association in db
        getGraphDB().dissociate(uaID, targetID, targetType);
        //create association in-memory
        getGraphMem().dissociate(uaID, targetID, targetType);
    }

    /**
     * Get the associations the given node is the source node of. First, check if the user is allowed to retrieve this
     * information.
     * @param sourceID The ID of the source node.
     * @return A map of the target ID and operations for each association the given node is the source of.
     */
    @Override
    public HashMap<Long, HashSet<String>> getSourceAssociations(long sourceID) throws LoaderException, SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException, NodeNotFoundException, InvalidProhibitionSubjectTypeException {
        if(!exists(sourceID)) {
            throw new NodeNotFoundException(sourceID);
        }

        //check the user can get the associations of the source node
        PolicyDecider decider = new MemPolicyDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(), sourceID, GET_ASSOCIATIONS)){
            throw new MissingPermissionException(sourceID, GET_ASSOCIATIONS);
        }

        return getGraphMem().getSourceAssociations(sourceID);
    }

    /**
     * Get the associations the given node is the target node of. First, check if the user is allowed to retrieve this
     * information.
     * @param targetID The ID of the source node.
     * @return A map of the source ID and operations for each association the given node is the target of.
     */
    @Override
    public HashMap<Long, HashSet<String>> getTargetAssociations(long targetID) throws LoaderException, SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException, NodeNotFoundException, InvalidProhibitionSubjectTypeException {
        if(!exists(targetID)) {
            throw new NodeNotFoundException(targetID);
        }

        //check the user can get the associations of the source node
        PolicyDecider decider = new MemPolicyDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(), targetID, GET_ASSOCIATIONS)){
            throw new MissingPermissionException(targetID, GET_ASSOCIATIONS);
        }

        return getGraphMem().getTargetAssociations(targetID);
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
     * @return A Response with the nodes that match the given search criteria.
     * @throws LoadConfigException If the PAP was unable to load the database configuration.
     * @throws LoaderException If the PAP was unable to load the graph into memory.
     * @throws DatabaseException If there is an error communicating with the database or retrieving information from the database.
     * @throws SessionDoesNotExistException If the session ID provided does not exist.
     * @throws MissingPermissionException If the given user is not allowed to perform the operation.
     */
    @Override
    public HashSet<Node> search(String name, String type, Map<String, String> properties) throws DatabaseException, LoadConfigException, LoaderException, SessionDoesNotExistException, MissingPermissionException, NodeNotFoundException, InvalidProhibitionSubjectTypeException {
        // user the PIP searcher to search for the intended nodes
        HashSet<Node> nodes = getSearch().search(name, type, properties);
        //filter out any nodes the user doesn't have any permissions on
        return new MemPolicyDecider(getGraphMem(), getProhibitionsMem().getProhibitions()).filter(getSessionUserID(), getProcessID(), nodes);
    }

    /**
     * Retrieve the node from the graph with the given ID.
     *
     * @param id the ID of the node to get.
     * @return The Node retrieved from the graph with the given ID
     * @throws NodeNotFoundException If the provided Node ID does not exist in the graph.
     * @throws DatabaseException If there is an error communicating with the database or retrieving information from the database.
     * @throws InvalidNodeTypeException If the provided Node type is invalid.
     * @throws SessionDoesNotExistException  If the current session ID does not exist.
     * @throws LoadConfigException If the PAP was unable to load the database configuration.
     * @throws LoaderException If the PAP was unable to load the graph into memory.
     * @throws MissingPermissionException If the given user is not allowed to perform the operation.
     */
    @Override
    public Node getNode(long id) throws NodeNotFoundException, DatabaseException, InvalidNodeTypeException, SessionDoesNotExistException, LoadConfigException, LoaderException, MissingPermissionException, InvalidProhibitionSubjectTypeException {
        if(!exists(id)) {
            throw new NodeNotFoundException(id);
        }

        PolicyDecider decider = new MemPolicyDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(), id, ANY_OPERATIONS)) {
            throw new MissingPermissionException(id, ANY_OPERATIONS);
        }

        return getSearch().getNode(id);
    }
}
