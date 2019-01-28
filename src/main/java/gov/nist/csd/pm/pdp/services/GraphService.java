package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.common.model.graph.relationships.NGACAssignment;
import gov.nist.csd.pm.common.model.graph.relationships.NGACAssociation;
import gov.nist.csd.pm.pap.graph.MemGraph;
import gov.nist.csd.pm.pap.search.MemGraphSearch;
import gov.nist.csd.pm.pdp.engine.Decider;
import gov.nist.csd.pm.pdp.engine.PReviewDecider;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static gov.nist.csd.pm.common.constants.Operations.*;
import static gov.nist.csd.pm.common.constants.Properties.NAMESPACE_PROPERTY;
import static gov.nist.csd.pm.common.constants.Properties.PASSWORD_PROPERTY;
import static gov.nist.csd.pm.common.constants.Properties.REP_PROPERTY;
import static gov.nist.csd.pm.common.model.graph.nodes.Node.generatePasswordHash;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.OA;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.PC;
import static gov.nist.csd.pm.pap.PAP.getPAP;

/**
 * GraphService provides methods to maintain an NGAC graph, while also ensuring any user interacting with the graph,
 * has the correct permissions to do so.
 */
public class GraphService extends Service implements Graph, Search {

    public GraphService(String sessionID, long processID) throws PMException {
        super(sessionID, processID);
    }

    /**
     * Create a node and assign it to the node with the given ID. The name and type must not be null.
     * This method is needed because if a node is created without an initial assignment, it will be impossible
     * to assign the node in the future since no user will have permissions on a node not connected to the graph.
     * In this method we can check the user has the permission to assign to the given parent node and ignore if
     * the user can assign the newly created node.
     *
     * @param parentID The ID of the node to assign the new node to.
     * @param ctx The Node to create.
     * @return The new node created with it's ID.
     */
    public Node createNode(long parentID, Node ctx) throws PMException {
        if(ctx == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "a null node was provided when creating a node in the PDP");
        }

        //if this node is a user, hash the password if present in the properties
        HashMap<String, String> properties = ctx.getProperties();
        if(properties.containsKey(PASSWORD_PROPERTY)) {
            try {
                properties.put(PASSWORD_PROPERTY, generatePasswordHash(properties.get(PASSWORD_PROPERTY)));
            }
            catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new PMException(Errors.ERR_HASHING_USER_PSWD, e.getMessage());
            }
        }

        // if the node is a policy class, check that the user has the "create policy class" permission on super o
        if(ctx.getType().equals(PC)) {
            Decider decider = newPolicyDecider();
            if (!decider.hasPermissions(getSessionUserID(), getProcessID(), getPAP().getSuperO()
                    .getID(), CREATE_POLICY_CLASS)) {
                throw new PMException(Errors.ERR_MISSING_PERMISSIONS, "missing permissions to create a Policy Class");
            }

            // create a node to represent the policy class in the super policy class
            // this way we can set permissions on this node to control who can (de)assign to the policy class node.
            long repID = createNode(new Node(ctx.getName() + " rep", OA, Node.toProperties(NAMESPACE_PROPERTY, ctx.getName())));

            // add the ID of the rep node to the properties of the policy class node
            ctx.property(REP_PROPERTY, String.valueOf(repID));
            // create pc node
            long id = createNode(ctx);
            ctx.id(id);

            // assign the rep object in the super pc
            //create assignment in db
            getGraphDB().assign(repID, OA, getPAP().getSuperOA().getID(), OA);
            //create assignment in-memory
            getGraphMem().assign(repID, OA, getPAP().getSuperOA().getID(), OA);
        } else {
            //check that the parent node exists
            if(!exists(parentID)) {
                throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node with ID %d does not exist", parentID));
            }

            //create the node
            long id = createNode(ctx);
            ctx.id(id);

            //get the parent node to make the assignment
            Node parentNode = getNode(parentID);

            // check that the user has the permission to assign to the parent node
            Decider decider = new PReviewDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
            if (!decider.hasPermissions(getSessionUserID(), getProcessID(), parentNode.getID(), ASSIGN_TO)) {
                // if the user cannot assign to the parent node, delete the newly created node
                getGraphDB().deleteNode(id);
                getGraphMem().deleteNode(id);
                throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permission %s on node with ID %d", ASSIGN_TO, parentID));
            }

            // make the assignments
            // create assignment in db
            getGraphDB().assign(ctx.getID(), ctx.getType(), parentNode.getID(), parentNode.getType());
            // create assignment in-memory
            getGraphMem().assign(ctx.getID(), ctx.getType(), parentNode.getID(), parentNode.getType());
        }

        return ctx;
    }

    /**
     * Create a new node in the database and in memory graphs.  No need to check permissions here, assigning the node is
     * where we check the user has the correct permissions.
     *
     * @param node The context of the node to create.  This includes the id, name, type, and properties.
     * @return The Node representing the node that was just created.
     */
    @Override
    public long createNode(Node node) throws PMException {
        if(node == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "a null node was provided when creating a node in the PDP");
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
     */
    @Override
    public void updateNode(Node node) throws PMException {
        if(node == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "a null node was provided when updating a node in the PDP");
        } else if (!exists(node.getID())) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node with ID %d could not be found", node.getID()));
        }

        //update node in database
        getGraphDB().updateNode(node);
        //update node in in-memory graph
        getGraphMem().updateNode(node);
    }

    /**
     * Delete the node with the given ID from the db and in-memory graphs.  First check that the current user
     * has the correct permissions to do so. Do this by checking that the user has the permission to deassign from each
     * of the node's parents, and that the user can delete the node.  If the node is a Policy Class or is assigned to a
     * Policy Class, check the permissions on the representative node.
     * @param nodeID the ID of the node to delete.
     */
    @Override
    public void deleteNode(long nodeID) throws PMException {
        MemGraphSearch search = new MemGraphSearch((MemGraph) getGraphMem());
        Node node = search.getNode(nodeID);

        if(node.getType().equals(PC)) {
            // if the node to delete is a PC, get the rep node
            nodeID = Long.parseLong(node.getProperties().get(REP_PROPERTY));
        }

        Decider decider = new PReviewDecider(getGraphMem(), getProhibitionsMem().getProhibitions());

        // check the user can deassign the node
        if (!decider.hasPermissions(getSessionUserID(), getProcessID(), nodeID, DEASSIGN)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %d: %s", nodeID, DEASSIGN));
        }

        // check that the user can deassign from the node's parents
        HashSet<Node> parents = getGraphMem().getParents(nodeID);
        for(Node parent : parents) {
            // check the user can deassign from parent
            // if the parent is a policy class, get the rep oa
            long targetID;
            if(parent.getType().equals(PC)) {
                targetID = Long.parseLong(parent.getProperties().get(REP_PROPERTY));
            } else {
                targetID = parent.getID();
            }
            if(!decider.hasPermissions(getSessionUserID(), getProcessID(), targetID, DEASSIGN_FROM)) {
                throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %d: %s", targetID, DEASSIGN_FROM));
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
     */
    @Override
    public boolean exists(long nodeID) throws PMException {
        return getGraphMem().exists(nodeID);
    }

    /**
     * Retrieve the list of all nodes in the graph.  Go to the database to do this, since it is more likely to have
     * all of the node information.
     * @return The set of all nodes in the graph.
     */
    @Override
    public HashSet<Node> getNodes() throws PMException {
        return getGraphDB().getNodes();
    }

    /**
     * Get the set of policy class IDs. This can be performed by the in-memory graph.
     * @return The set of IDs for the policy classes in the graph.
     */
    @Override
    public HashSet<Long> getPolicies() throws PMException {
        return getGraphMem().getPolicies();
    }

    /**
     * Get the children of the node from the graph.  Get the children from the database to ensure all node information
     * is present.  Before returning the set of nodes, filter out any nodes that the user has no permissions on.
     * @param nodeID The ID of the node to get the children of.

     */
    @Override
    public HashSet<Node> getChildren(long nodeID) throws PMException {
        if(!exists(nodeID)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("nodes with ID %d could not be found", nodeID));
        }

        //get the children from the db
        HashSet<Node> children = getGraphDB().getChildren(nodeID);
        //filter any nodes that the user doesn't have any permissions on
        return new PReviewDecider(getGraphMem(), getProhibitionsMem().getProhibitions()).filter(getSessionUserID(), getProcessID(), children, ANY_OPERATIONS);
    }

    /**
     * Get the parents of the node from the graph.  Get the parents from the database to ensure all node information
     * is present.  Before returning the set of nodes, filter out any nodes that the user has no permissions on.
     * @param nodeID The ID of the node to get the parents of.
     */
    @Override
    public HashSet<Node> getParents(long nodeID) throws PMException {
        if(!exists(nodeID)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("nodes with ID %d could not be found", nodeID));
        }

        //get the children from the db
        HashSet<Node> children = getGraphDB().getParents(nodeID);
        //filter any nodes that the user doesn't have any permissions on
        return new PReviewDecider(getGraphMem(), getProhibitionsMem().getProhibitions()).filter(getSessionUserID(), getProcessID(), children, ANY_OPERATIONS);
    }

    /**
     * Create the assignment in both the db and in-memory graphs. First check that the user is allowed to assign the child,
     * and allowed to assign something to the parent.
     * @param childID The ID of the child node.
     * @param childType The type of the child node.
     * @param parentID The the ID of the parent node.
     * @param parentType The type of the parent node.
     */
    @Override
    public void assign(long childID, NodeType childType, long parentID, NodeType parentType) throws PMException {
        //check that the nodes exist
        if(!exists(childID)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("child node with ID %d does not exist", childID));
        }
        if(!exists(parentID)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("parent node with ID %d does not exist", parentID));
        }

        //check if the assignment is valid
        NGACAssignment.checkAssignment(childType, parentType);

        //check the user can assign the child
        Decider decider = new PReviewDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(), childID, ASSIGN)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permission %s on node with ID %d", ASSIGN, childID));
        }

        //check that the user can assign to the parent
        // if the parent is a PC, check for permissions on the rep node
        long targetID = parentID;
        if(parentType.equals(PC)) {
            // get the policy class node
            Node node = new MemGraphSearch((MemGraph) getGraphMem()).getNode(targetID);
            // get the rep property which is the ID of the rep node
            // set the target of the permission check to the rep node
            targetID = Long.parseLong(node.getProperties().get(REP_PROPERTY));
        }

        // check that the user can assign to the parent node
        if (!decider.hasPermissions(getSessionUserID(), getProcessID(), targetID, ASSIGN_TO)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permission %s on node with ID %d", ASSIGN_TO, targetID));
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
     */
    @Override
    public void deassign(long childID, NodeType childType, long parentID, NodeType parentType) throws PMException {
        //check nodes exist
        if(!exists(childID)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("nodes with ID %d could not be found", childID));
        }
        if(!exists(parentID)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("nodes with ID %d could not be found", parentID));
        }

        Decider decider = new PReviewDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        //check the user can deassign the child
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(), childID, DEASSIGN)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %d: %s", childID, DEASSIGN));
        }
        //check that the user can deassign from the parent
        // if the parent is a PC, check for permissions on the rep node
        long targetID = parentID;
        if(parentType.equals(PC)) {
            // get the policy class node
            Node node = new MemGraphSearch((MemGraph) getGraphMem()).getNode(targetID);
            // get the rep property which is the ID of the rep node
            // set the target of the permission check to the rep node
            targetID = Long.parseLong(node.getProperties().get(REP_PROPERTY));
        }

        if (!decider.hasPermissions(getSessionUserID(), getProcessID(), targetID, DEASSIGN_FROM)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %d: %s", targetID, DEASSIGN_FROM));
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
     */
    @Override
    public void associate(long uaID, long targetID, NodeType targetType, HashSet<String> operations) throws PMException {
        //check that the nodes exist and the operations are not null
        if(!exists(uaID)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("nodes with ID %d could not be found", uaID));
        }
        if(!exists(targetID)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("nodes with ID %d could not be found", targetID));
        }
        if(operations == null) {
            operations = new HashSet<>();
        }

        Node sourceNode = getNode(uaID);
        Node targetNode = getNode(targetID);

        NGACAssociation.checkAssociation(sourceNode.getType(), targetNode.getType());

        //check the user can associate the source and target nodes
        Decider decider = new PReviewDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(), uaID, ASSOCIATE)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %d: %s", uaID, ASSOCIATE));
        }
        if (!decider.hasPermissions(getSessionUserID(), getProcessID(), targetID, ASSOCIATE)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %d: %s", targetID, ASSOCIATE));
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
    public void dissociate(long uaID, long targetID, NodeType targetType) throws PMException {
        //check the user can associate the source and target nodes
        Decider decider = new PReviewDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(), uaID, DISASSOCIATE)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %d: %s", uaID, DISASSOCIATE));
        }
        if (!decider.hasPermissions(getSessionUserID(), getProcessID(), targetID, DISASSOCIATE)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %d: %s", targetID, DISASSOCIATE));
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
    public HashMap<Long, HashSet<String>> getSourceAssociations(long sourceID) throws PMException {
        if(!exists(sourceID)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("nodes with ID %d could not be found", sourceID));
        }

        //check the user can get the associations of the source node
        Decider decider = new PReviewDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(), sourceID, GET_ASSOCIATIONS)){
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %d: %s", sourceID, GET_ASSOCIATIONS));
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
    public HashMap<Long, HashSet<String>> getTargetAssociations(long targetID) throws PMException {
        if(!exists(targetID)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("nodes with ID %d could not be found", targetID));
        }

        //check the user can get the associations of the source node
        Decider decider = new PReviewDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(), targetID, GET_ASSOCIATIONS)){
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %d: %s", targetID, GET_ASSOCIATIONS));
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
     */
    @Override
    public HashSet<Node> search(String name, String type, Map<String, String> properties) throws PMException {
        // user the PIP searcher to search for the intended nodes
        HashSet<Node> nodes = getSearch().search(name, type, properties);
        //filter out any nodes the user doesn't have any permissions on
        return new PReviewDecider(getGraphMem(), getProhibitionsMem().getProhibitions()).filter(getSessionUserID(), getProcessID(), nodes);
    }

    /**
     * Retrieve the node from the graph with the given ID.
     *
     * @param id the ID of the node to get.
     * @return The Node retrieved from the graph with the given ID.
     */
    @Override
    public Node getNode(long id) throws PMException {
        if(!exists(id)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("nodes with ID %d could not be found", id));
        }

        Decider decider = new PReviewDecider(getGraphMem(), getProhibitionsMem().getProhibitions());
        if(!decider.hasPermissions(getSessionUserID(), getProcessID(), id, ANY_OPERATIONS)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %d: %s", id, ANY_OPERATIONS));
        }

        return getSearch().getNode(id);
    }
}
