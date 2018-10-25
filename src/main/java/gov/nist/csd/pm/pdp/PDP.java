package gov.nist.csd.pm.pdp;

import gov.nist.csd.pm.model.constants.MetaDataNodes;
import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pdp.engine.MemPolicyDecider;
import gov.nist.csd.pm.pdp.engine.PolicyDecider;
import gov.nist.csd.pm.model.graph.NGAC;
import gov.nist.csd.pm.pip.loader.LoaderException;
import gov.nist.csd.pm.model.graph.Search;

import java.util.*;

import static gov.nist.csd.pm.model.constants.Operations.*;
import static gov.nist.csd.pm.pip.PIP.getPIP;

/**
 * The PDP implements both NGAC and Search interfaces and is used by the PEP to manage NGAC data.
 */
public class PDP implements NGAC, Search {

    /**
     * The ID of the session currently using the PDP.
     */
    private String sessionID;

    /**
     * The ID of the process currently using the PDP.
     */
    private long processID;

    /**
     * Create a new PDP with a sessionID and processID
     * @param sessionID the ID of the current session.  This cannot be null.
     * @param processID the ID of the current process.  This can be 0.
     */
    public PDP(String sessionID, long processID) {
        if(this.sessionID == null || this.sessionID.isEmpty()) {
            throw new IllegalArgumentException("The session ID cannot be null or empty");
        }

        this.sessionID = sessionID;
        this.processID = processID;
    }

    /**
     * Get the ID of the current session.
     * @return The current session's ID.
     */
    protected String getSessionID() {
        return sessionID;
    }

    /**
     * Get the ID of the current process.  The current process can be 0, in which case a process
     * is not currently being used.
     * @return the ID of the current process.
     */
    long getProcessID() {
        return processID;
    }

    /**
     * Get a new instance of a PolicyDecider.  This is meant be called each time a decision is needed.
     * @return An instance of a PolicyDecider.
     */
    public PolicyDecider getPolicyDecider() throws LoaderException, SessionDoesNotExistException, LoadConfigException, DatabaseException {
        return new MemPolicyDecider(getPIP().getNGACBackend().getNGACMem(), getSessionUserID(), getProcessID());
    }

    /**
     * Get the Search instance from the PIP.
     * @return The implementation of the Search interface from the PIP
     */
    public Search getSearch() throws LoaderException, DatabaseException, LoadConfigException {
        return getPIP().getNGACBackend().getSearch();
    }

    /**
     * Get the NGAC database instance from the PIP.
     * @return The database implementation of the NGAC interface from the PIP
     */
    public NGAC getDB() throws LoaderException, DatabaseException, LoadConfigException {
        return getPIP().getNGACBackend().getDB();
    }

    /**
     * Get the NGAC in memory instance from the PIP.
     * @return The in memory implementation of the NGAC interface from the PIP
     */
    public NGAC getMem() throws LoaderException, DatabaseException, LoadConfigException {
        return getPIP().getNGACBackend().getDB();
    }

    /**
     * Get the ID of the User that is associated with the current session ID.
     * @return The ID of the user node.
     */
    public long getSessionUserID() throws LoaderException, DatabaseException, LoadConfigException, SessionDoesNotExistException {
        return getPIP().getSessionsDAO().getSessionUserID(sessionID);
    }

    /**
     * Create a new node in the database and in memory graphs.  No need to check permissions here, assigning the node is
     * where we check the user has the correct permissions.
     *
     * @param ctx The context of the node to create.  This includes the id, name, type, and properties.
     * @return The Node representing the node that was just created.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws LoadConfigException If the server cannot load the database configuration.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws NullNodeCtxException If the provided Node context is null.
     * @throws NoIDException If the given node context does not already have the ID.
     * @throws NullTypeException If the given node context does not have a type.
     * @throws NullNameException If the given node context does not have a name.
     */
    @Override
    public Node createNode(Node ctx) throws LoadConfigException, DatabaseException, LoaderException, NullNodeCtxException, NoIDException, NullTypeException, NullNameException {
        //create node in database
        Node node = getDB().createNode(ctx);

        //add node to in-memory graph
        getMem().createNode(node);

        return node;
    }

    /**
     * This method creates a new Policy Class in the NGAC graph.  Unlike other nodes, Policy Class nodes must have unique names.
     *
     * When a policy class is created, an object attribute and a user attribute of the same name are also created and
     * assigned to the policy class. The current user is then assigned to the user attribute and an association is
     * created to give the current user all operations (*) on the object attribute.  Meaning the user that created this
     * policy is permitted all operations on anything in this policy class. Finally, the super user attribute is
     * associated with the object attribute and user attribute created above, to give the super user all operations on
     * all objects/users in this policy class.
     *
     * Example: User u1 creates policy PC1
     * Nodes created:
     *   - Policy123 (PC)
     *   - Policy123 OA (OA)
     *   - Policy123 admin (UA)
     * Assignments created:
     *   - Policy123 OA -> Policy123
     *   - Policy123 admin -> Policy123
     *   - Super UA -> Policy123
     *   - u1 -> Policy123 admin
     * Associations created:
     *   - Policy123 admin -{*}-> Policy123 OA
     *   - Super UA -{*}-> Policy123 OA
     *   - Super UA -{*}-> Policy123 admin
     *
     * @param ctx The context of the policy class to create.
     * @return The Policy Class Object Attribute. This is because once this method ends no user, not even super, can assign
     * nodes to the Policy Class.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws SessionDoesNotExistException If the current session ID does not exist.
     * @throws LoadConfigException If the server cannot load the database configuration.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws NullNodeCtxException If the provided Node context is null.
     * @throws NoIDException If the node context does not have an ID when creating a node in the in-memory graph
     * @throws NullTypeException If the given node context does not have a type.
     * @throws NullNameException If the given node context does not have a name.
     * @throws MissingPermissionException If the current user does not the correct permissions.
     */
    public Node createPolicy(Node ctx) throws LoaderException, SessionDoesNotExistException, LoadConfigException, DatabaseException,
            NullNodeCtxException, NullTypeException, NullNameException, MissingPermissionException, NoIDException {
        //check that neither of the ctxs are null to avoid NPE
        if(ctx == null) {
            throw new NullNodeCtxException();
        }

        //create the PC node
        Node pcNode = createNode(ctx);

        //create OA
        Node oaCtx = new Node(pcNode.getName() + " OA", NodeType.OA, pcNode.getProperties());
        Node oaNode = createNode(oaCtx);

        //assign OA to PC
        assignInBackend(oaNode, pcNode);

        //create UA
        Node uaCtx = new Node(pcNode.getName() + " admin", NodeType.UA, pcNode.getProperties());
        Node uaNode = createNode(uaCtx);

        //assign UA to PC
        assignInBackend(uaNode, pcNode);

        //assign U to UA
        assignInBackend(new Node(getSessionUserID(), NodeType.U), uaNode);

        //create association for the admin UA
        associateInBackend(uaNode.getID(), oaNode.getID(), new HashSet<>(Collections.singleton(ALL_OPERATIONS)));

        //assign the super UA to this policy
        assignInBackend(new Node(MetaDataNodes.SUPER_UA_ID, NodeType.UA), ctx);
        //create association for super UA which is a meta data node
        //this will give the Super User all operations on the everything in this policy
        associateInBackend(MetaDataNodes.SUPER_UA_ID, oaNode.getID(), new HashSet<>(Collections.singleton(ALL_OPERATIONS)));
        //create an association between the Super UA and the PC admin
        associateInBackend(MetaDataNodes.SUPER_UA_ID, uaNode.getID(), new HashSet<>(Collections.singleton(ALL_OPERATIONS)));

        //return the Object Attribute representing the Policy Class
        //because this is the node that all future nodes will need to be assigned to
        //no user, even super, can assign anything to the policy class once this method is done.
        return oaNode;
    }

    /**
     * Update the node in the database and in the in-memory graph
     * @param ctx The context of the node to update. This includes the id, name, type, and properties.
     * @throws NullNodeCtxException If either of the provided Node contexts are null.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws LoadConfigException If the server cannot load the database configuration.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws NullNodeCtxException If the provided Node context is null.
     */
    @Override
    public void updateNode(Node ctx) throws LoadConfigException, DatabaseException, LoaderException, NullNodeCtxException, NoIDException {
        //update node in database
        getDB().updateNode(ctx);

        //update node in in-memory graph
        getMem().updateNode(ctx);
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
    public void deleteNode(long nodeID) throws LoaderException, SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException {
        //check that the user can delete the node
        PolicyDecider decider = getPolicyDecider();
        if (!decider.hasPermissions(nodeID, DELETE_NODE)) {
            throw new MissingPermissionException(nodeID, DELETE_NODE);
        }
        //check the user can deassign the node
        if (!decider.hasPermissions(nodeID, DEASSIGN)) {
            throw new MissingPermissionException(nodeID, DEASSIGN);
        }

        //check that the user can deassign from the node's parents
        HashSet<Node> parents = getMem().getParents(nodeID);
        for(Node parent : parents) {
            //check the user can deassign from parent
            if(!decider.hasPermissions(parent.getID(), DEASSIGN_FROM)) {
                throw new MissingPermissionException(parent.getID(), DEASSIGN_FROM);
            }
        }

        //if all checks pass, delete the node, thus deleting assignments
        //delete the node in db
        getDB().deleteNode(nodeID);

        //delete the node in-memory
        getMem().deleteNode(nodeID);
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
    public boolean exists(long nodeID) throws LoadConfigException, DatabaseException, LoaderException {
        return getMem().exists(nodeID);
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
    public HashSet<Node> getNodes() throws LoadConfigException, DatabaseException, LoaderException {
        return getDB().getNodes();
    }

    /**
     * Get the set of policy class IDs. This can be performed by the in-memory graph.
     * @return The set of IDs for the policy classes in the graph.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws LoadConfigException If the server cannot load the database configuration.
     */
    @Override
    public HashSet<Long> getPolicies() throws LoadConfigException, DatabaseException, LoaderException {
        return getMem().getPolicies();
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
    public HashSet<Node> getChildren(long nodeID) throws LoaderException, SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException {
        //get the children from the db
        HashSet<Node> children = getDB().getChildren(nodeID);
        //filter any nodes that the user doesn't have any permissions on
        return getPolicyDecider().filter(children, ANY_OPERATIONS);
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
    public HashSet<Node> getParents(long nodeID) throws LoadConfigException, DatabaseException, LoaderException, SessionDoesNotExistException, MissingPermissionException {
        //get the children from the db
        HashSet<Node> children = getDB().getParents(nodeID);
        //filter any nodes that the user doesn't have any permissions on
        return getPolicyDecider().filter(children, ANY_OPERATIONS);
    }

    /**
     * Create the assignment in both the db and in-memory graphs. First check that the user is allowed to assign the child,
     * and allowed to assign something to the parent.
     * @param childCtx The child ID and type.
     * @param parentCtx The parent ID and type.
     * @throws NullNodeCtxException If either of the provided Node contexts are null.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws SessionDoesNotExistException If the current session ID does not exist.
     * @throws LoadConfigException If the server cannot load the database configuration.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws MissingPermissionException If the current user does not the correct permissions.
     * @throws NullTypeException If either of the provided contexts have a null type.
     */
    @Override
    public void assign(Node childCtx, Node parentCtx) throws NullNodeCtxException, LoaderException, SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException, NullTypeException {
        //check that neither of the ctxs are null to avoid NPE
        if(childCtx == null || parentCtx == null) {
            throw new NullNodeCtxException();
        }

        long childID = childCtx.getID();
        long parentID = parentCtx.getID();

        PolicyDecider decider = getPolicyDecider();
        //check the user can assign the child
        if(!decider.hasPermissions(childID, ASSIGN)) {
            throw new MissingPermissionException(childID, ASSIGN);
        }
        //check that the user can assign to the parent
        if(!decider.hasPermissions(parentID, ASSIGN_TO)) {
            throw new MissingPermissionException(parentID, ASSIGN_TO);
        }

        //create the assignment in the backend
        assignInBackend(childCtx, parentCtx);
    }

    private void assignInBackend(Node childCtx, Node parentCtx) throws LoadConfigException, DatabaseException, LoaderException, NullNodeCtxException, SessionDoesNotExistException, NullTypeException, MissingPermissionException {
        //create assignment in db
        getDB().assign(childCtx, parentCtx);
        //create assignment in-memory
        getMem().assign(childCtx, parentCtx);
    }

    /**
     * Create the assignment in both the db and in-memory graphs. First check that the user is allowed to assign the child,
     * and allowed to assign something to the parent.
     * @param childCtx The ID and type of the child node.
     * @param parentCtx The ID and type of the parent node.
     * @throws NullNodeCtxException If either of the provided Node contexts are null.
     * @throws LoaderException If there is an error loading the graph into memory.
     * @throws SessionDoesNotExistException If the current session ID does not exist.
     * @throws LoadConfigException If the server cannot load the database configuration.
     * @throws DatabaseException If there is an error performing this action in the database or connecting to the database.
     * @throws MissingPermissionException If the current user does not the correct permissions.
     * @throws NullTypeException If either of the provided contexts have a null type.
     */
    @Override
    public void deassign(Node childCtx, Node parentCtx) throws LoaderException, SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException, NullNodeCtxException, NullTypeException {
        //check that neither of the ctxs are null to avoid NPE
        if(childCtx == null || parentCtx == null) {
            throw new NullNodeCtxException();
        }

        PolicyDecider decider = getPolicyDecider();
        //check the user can deassign the child
        if(!decider.hasPermissions(childCtx.getID(), DEASSIGN)) {
            throw new MissingPermissionException(childCtx.getID(), DEASSIGN);
        }
        //check that the user can deassign from the parent
        if(!decider.hasPermissions(parentCtx.getID(), DEASSIGN_FROM)) {
            throw new MissingPermissionException(parentCtx.getID(), DEASSIGN);
        }

        //delete the assignment in the backend
        deassignInBackend(childCtx, parentCtx);
    }

    private void deassignInBackend(Node childCtx, Node parentCtx) throws LoadConfigException, DatabaseException, LoaderException, NullNodeCtxException, SessionDoesNotExistException, NullTypeException, MissingPermissionException {
        //delete assignment in db
        getDB().deassign(childCtx, parentCtx);
        //delete assignment in-memory
        getMem().deassign(childCtx, parentCtx);
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
    public void associate(long uaID, long targetID, HashSet<String> operations) throws DatabaseException, LoadConfigException, LoaderException, MissingPermissionException, SessionDoesNotExistException {
        //check the user can associate the source and target nodes
        PolicyDecider decider = getPolicyDecider();
        if(!decider.hasPermissions(uaID, ASSOCIATE)) {
            throw new MissingPermissionException(uaID, ASSOCIATE);
        }
        if (!decider.hasPermissions(targetID, ASSOCIATE)) {
            throw new MissingPermissionException(targetID, ASSOCIATE);
        }

        //create the association in the backend
        associateInBackend(uaID, targetID, operations);
    }

    private void associateInBackend(long uaID, long targetID, HashSet<String> operations) throws LoadConfigException, DatabaseException, LoaderException, SessionDoesNotExistException, MissingPermissionException {
        //create association in db
        getDB().associate(uaID, targetID, operations);
        //create association in-memory
        getMem().associate(uaID, targetID, operations);
    }

    /**
     * Delete the association between the user attribute and the target node.  First, check that the user has the
     * permission to delete the association.
     * @param uaID The ID of the User Attribute.
     * @param targetID The ID of the target node.
     */
    @Override
    public void dissociate(long uaID, long targetID) throws LoaderException, SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException {
        //check the user can associate the source and target nodes
        PolicyDecider decider = getPolicyDecider();
        if(!decider.hasPermissions(uaID, DISASSOCIATE)) {
            throw new MissingPermissionException(uaID, DISASSOCIATE);
        }
        if (!decider.hasPermissions(targetID, DISASSOCIATE)) {
            throw new MissingPermissionException(targetID, DISASSOCIATE);
        }

        //delete the association in the backend
        dissociateInBackend(uaID, targetID);
    }

    private void dissociateInBackend(long uaID, long targetID) throws LoadConfigException, DatabaseException, LoaderException, SessionDoesNotExistException, MissingPermissionException {
        //create association in db
        getDB().dissociate(uaID, targetID);
        //create association in-memory
        getMem().dissociate(uaID, targetID);
    }

    /**
     * Get the associations the given node is the source node of. First, check if the user is allowed to retrieve this
     * information.
     * @param sourceID The ID of the source node.
     * @return A map of the target ID and operations for each association the given node is the source of.
     */
    @Override
    public HashMap<Long, HashSet<String>> getSourceAssociations(long sourceID) throws LoaderException, SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException {
        //check the user can get the associations of the source node
        PolicyDecider decider = getPolicyDecider();
        if(!decider.hasPermissions(sourceID, GET_ASSOCIATIONS)){
            throw new MissingPermissionException(sourceID, GET_ASSOCIATIONS);
        }

        return getMem().getSourceAssociations(sourceID);
    }

    /**
     * Get the associations the given node is the target node of. First, check if the user is allowed to retrieve this
     * information.
     * @param targetID The ID of the source node.
     * @return A map of the source ID and operations for each association the given node is the target of.
     */
    @Override
    public HashMap<Long, HashSet<String>> getTargetAssociations(long targetID) throws LoaderException, SessionDoesNotExistException, LoadConfigException, DatabaseException, MissingPermissionException {
        //check the user can get the associations of the source node
        PolicyDecider decider = getPolicyDecider();
        if(!decider.hasPermissions(targetID, GET_ASSOCIATIONS)){
            throw new MissingPermissionException(targetID, GET_ASSOCIATIONS);
        }

        return getMem().getTargetAssociations(targetID);
    }

    /**
     * Search the NGAC graph for nodes that match the given parameters.  Use the PIP's search interface to search for
     * the nodes and then filter out any nodes the current user doesn't have any permissions on.
     * @param name The name of the nodes to search for.
     * @param type The type of the nodes to search for.
     * @param properties The properties of the nodes to search for.
     * @return The set of nodes that match the provided search criteria.
     */
    @Override
    public HashSet<Node> search(String name, String type, Map<String, String> properties) throws DatabaseException, LoadConfigException, LoaderException, SessionDoesNotExistException, MissingPermissionException {
        // user the PIP searcher to search for the intended nodes
        HashSet<Node> nodes = getSearch().search(name, type, properties);
        //filter out any nodes the user doesn't have any permissions on
        return getPolicyDecider().filter(nodes);
    }

    @Override
    public Node getNode(long id) throws NodeNotFoundException, DatabaseException, InvalidNodeTypeException, SessionDoesNotExistException, LoadConfigException, LoaderException, MissingPermissionException {
        PolicyDecider decider = getPolicyDecider();
        if(!decider.hasPermissions(id, ANY_OPERATIONS)) {
            throw new MissingPermissionException(id, ANY_OPERATIONS);
        }

        return getSearch().getNode(id);
    }
}
