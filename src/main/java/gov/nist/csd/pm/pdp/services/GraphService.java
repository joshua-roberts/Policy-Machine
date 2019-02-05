package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.common.model.graph.nodes.NodeUtils;
import gov.nist.csd.pm.common.model.graph.relationships.Assignment;
import gov.nist.csd.pm.common.model.graph.relationships.Association;
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
import static gov.nist.csd.pm.common.constants.Properties.*;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeUtils.generatePasswordHash;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.OA;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.PC;
import static gov.nist.csd.pm.pap.PAP.getPAP;

/**
 * GraphService provides methods to maintain an NGAC graph, while also ensuring any user interacting with the graph,
 * has the correct permissions to do so.
 */
public class GraphService extends Service implements Graph, Search {

    public GraphService(long userID, long processID) throws PMException {
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
     * @param ctx The context information for the node to create.  This includes the parentID, name, type, and properties.
     * @return The new node created with it's ID.
     * @throws PMException If the NodeContext parameter is null.
     * @throws PMException If the NodeContext's name field is null or empty.
     * @throws PMException If the NodeContext's type field is null.
     * @throws PMException If a node already exists with the given name, type, and namespace property.
     * @throws PMException If the current user does not have permission to create the node.
     */
    @Override
    public long createNode(NodeContext ctx) throws PMException {
        if(ctx == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "a null request was provided when creating a node in the PDP");
        } else if (ctx.getName() == null || ctx.getName().isEmpty()) {
            throw new PMException(Errors.ERR_NULL_NAME, "a node cannot have a null or empty name");
        } else if (ctx.getType() == null) {
            throw new PMException(Errors.ERR_NULL_NAME, "a node cannot have a null type");
        }

        // instantiate the properties map if it's null
        // if this node is a user, hash the password if present in the properties
        HashMap<String, String> properties = ctx.getProperties();
        if(properties.containsKey(PASSWORD_PROPERTY)) {
            try {
                properties.put(PASSWORD_PROPERTY, generatePasswordHash(properties.get(PASSWORD_PROPERTY)));
            }
            catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new PMException(Errors.ERR_HASHING_USER_PSWD, e.getMessage());
            }
        }
        // check that the intended namespace does not already have the node name
        String namespace = properties.get(NAMESPACE_PROPERTY);
        if(namespace == null) {
            namespace = DEFAULT_NAMESPACE;
        }
        // search the graph for any node with the same name, type, and namespace
        HashSet<NodeContext> search = getGraphPAP().search(ctx.getName(), ctx.getType().toString(),
                NodeUtils.toProperties(NAMESPACE_PROPERTY, namespace));
        if(!search.isEmpty()) {
            throw new PMException(Errors.ERR_NODE_NAME_EXISTS_IN_NAMESPACE,
                    String.format("a node with the name \"%s\" and type %s already exists in the namespace \"%s\"",
                            ctx.getName(), ctx.getType(), namespace));
        }

        // if the node is a policy class, check that the user has the "create policy class" permission on super o
        if(ctx.getType().equals(PC)) {
            Decider decider = getDecider();
            if (!decider.hasPermissions(getUserID(), getProcessID(), getPAP().getSuperO().getID(), CREATE_POLICY_CLASS)) {
                throw new PMException(Errors.ERR_MISSING_PERMISSIONS, "missing permissions to create a Policy Class");
            }

            // create a node to represent the policy class in the super policy class
            // this way we can set permissions on this node to control who can (de)assign to the policy class node.
            HashMap<String, String> repProps = new HashMap<>();
            ctx.getProperties().forEach(repProps::putIfAbsent);
            NodeContext repCtx = new NodeContext()
                    .name(ctx.getName() + " rep")
                    .type(OA)
                    .properties(repProps);
            long repID = getGraphPAP().createNode(repCtx);
            // add the ID to the rep node context to assign later
            repCtx.id(repID);

            // add the ID of the rep node to the properties of the policy class node
            ctx.property(REP_PROPERTY, String.valueOf(repID));
            // create pc node
            long id = getGraphPAP().createNode(ctx);
            // set the ID of the created policy class node
            ctx.id(id);

            // assign the rep object in the super pc
            getGraphPAP().assign(repCtx, new NodeContext(getPAP().getSuperOA().getID(), OA));
        } else {
            //check that the parent node exists
            if(!exists(ctx.getParentID())) {
                throw new PMException(Errors.ERR_NODE_NOT_FOUND,
                        String.format("the specified parent node with ID %d does not exist", ctx.getParentID()));
            }

            //get the parent node to make the assignment
            NodeContext parentNodeCtx = getNode(ctx.getParentID());

            // check if the parent is a PC and get the rep if it is
            long parentID = parentNodeCtx.getID();
            if(parentNodeCtx.getType().equals(PC)) {
                parentID = Long.parseLong(parentNodeCtx.getProperties().get(REP_PROPERTY));
            }

            // check that the user has the permission to assign to the parent node
            Decider decider = getDecider();
            if (!decider.hasPermissions(getUserID(), getProcessID(), parentID, ASSIGN_TO)) {
                // if the user cannot assign to the parent node, delete the newly created node
                throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permission %s on node with ID %d", ASSIGN_TO, parentID));
            }

            //create the node
            long id = getGraphPAP().createNode(ctx);
            ctx.id(id);

            // assign the node to the specified parent node
            getGraphPAP().assign(ctx, parentNodeCtx);
        }

        // return the node ID
        return ctx.getID();
    }

    /**
     * Update the node in the database and in the in-memory graph
     * @param node The context of the node to update. This includes the id, name, type, and properties.
     * @throws PMException If the given node is null.
     * @throws PMException If the given node does not have an ID.
     */
    @Override
    public void updateNode(NodeContext node) throws PMException {
        if(node == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "a null node was provided when updating a node in the PDP");
        } else if(node.getID() == 0) {
            throw new PMException(Errors.ERR_NO_ID, "no ID was provided when updating the node");
        } else if (!exists(node.getID())) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node with ID %d could not be found", node.getID()));
        }

        // check that the user can update the node
        Decider decider = getDecider();
        if(!decider.hasPermissions(getUserID(), getProcessID(), node.getID(), UPDATE_NODE)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permission %s on node with ID %d", UPDATE_NODE, node.getID()));
        }

        //update node in the PAP
        getGraphPAP().updateNode(node);
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
        NodeContext node = getGraphPAP().getNode(nodeID);

        long repID = 0;
        long targetID = nodeID;
        if(node.getType().equals(PC)) {
            // if the node to delete is a PC, get the rep node
            targetID = Long.parseLong(node.getProperties().get(REP_PROPERTY));
            repID = targetID;
        }

        Decider decider = getDecider();

        // check the user can deassign the node
        if (!decider.hasPermissions(getUserID(), getProcessID(), targetID, DEASSIGN)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %s: %s", nodeID, DEASSIGN));
        }

        // check that the user can deassign from the node's parents
        HashSet<NodeContext> parents = getGraphPAP().getParents(nodeID);
        for(NodeContext parent : parents) {
            // check the user can deassign from parent
            // if the parent is a policy class, get the rep oa
            if(parent.getType().equals(PC)) {
                targetID = Long.parseLong(parent.getProperties().get(REP_PROPERTY));
            } else {
                targetID = parent.getID();
            }
            if(!decider.hasPermissions(getUserID(), getProcessID(), targetID, DEASSIGN_FROM)) {
                throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %s: %s", targetID, DEASSIGN_FROM));
            }
        }

        //if all checks pass, delete the node, thus deleting assignments
        //delete the node in the PAP
        getGraphPAP().deleteNode(nodeID);
        // if there is a rep ID, delete the rep
        if(repID == 0) {
            getGraphPAP().deleteNode(repID);
        }
    }

    /**
     * Check that a node with the given ID exists.  Just checking the in-memory graph is faster.
     * @param nodeID the ID of the node to check for.
     * @return True if a node with the given ID exists, false otherwise.
     */
    @Override
    public boolean exists(long nodeID) throws PMException {
        return getGraphPAP().exists(nodeID);
    }

    /**
     * Retrieve the list of all nodes in the graph.  Go to the database to do this, since it is more likely to have
     * all of the node information.
     * @return The set of all nodes in the graph.
     */
    @Override
    public HashSet<NodeContext> getNodes() throws PMException {
        return getDecider().filter(getUserID(), getProcessID(), getGraphPAP().getNodes(), ANY_OPERATIONS);
    }

    /**
     * Get the set of policy class IDs. This can be performed by the in-memory graph.
     * @return The set of IDs for the policy classes in the graph.
     */
    @Override
    public HashSet<Long> getPolicies() throws PMException {
        return getGraphPAP().getPolicies();
    }

    /**
     * Get the children of the node from the graph.  Get the children from the database to ensure all node information
     * is present.  Before returning the set of nodes, filter out any nodes that the user has no permissions on.
     * @param nodeID The ID of the node to get the children of.

     */
    @Override
    public HashSet<NodeContext> getChildren(long nodeID) throws PMException {
        if(!exists(nodeID)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node with ID %d could not be found", nodeID));
        }

        //get the children from the db
        HashSet<NodeContext> children = getGraphPAP().getChildren(nodeID);
        //filter any nodes that the user doesn't have any permissions on
        return getDecider().filter(getUserID(), getProcessID(), children, ANY_OPERATIONS);
    }

    /**
     * Get the parents of the node from the graph.  Get the parents from the database to ensure all node information
     * is present.  Before returning the set of nodes, filter out any nodes that the user has no permissions on.
     * @param nodeID The ID of the node to get the parents of.
     */
    @Override
    public HashSet<NodeContext> getParents(long nodeID) throws PMException {
        if(!exists(nodeID)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node with ID %d could not be found", nodeID));
        }

        //get the children from the db
        HashSet<NodeContext> children = getGraphPAP().getParents(nodeID);
        //filter any nodes that the user doesn't have any permissions on
        return getDecider().filter(getUserID(), getProcessID(), children, ANY_OPERATIONS);
    }

    /**
     * Create the assignment in both the db and in-memory graphs. First check that the user is allowed to assign the child,
     * and allowed to assign something to the parent. Both child and parent contexts must include the ID and type of the node.
     * @param childCtx The context information for the child node.
     * @param parentCtx The context information for the parent node.
     * @throws PMException If the child node is null.
     * @throws PMException If the parent node is null.
     * @throws PMException If the child node does not have an ID or type.
     * @throws PMException If the parent node does not have an ID or type.
     * @throws PMException If the child node does not exist.
     * @throws PMException If the parent node does not exist.
     * @throws PMException If the assignment is invalid.
     * @throws PMException If the current user does not have permission to create the assignment.
     */
    @Override
    public void assign(NodeContext childCtx, NodeContext parentCtx) throws PMException {
        // check that the nodes are not null
        if(childCtx == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "child node was null");
        } else if(parentCtx == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "parent node was null");
        } else if(childCtx.getID() == 0) {
            throw new PMException(Errors.ERR_NO_ID, "the child node ID cannot be 0 when creating an assignment");
        } else if(childCtx.getType() == null) {
            throw new PMException(Errors.ERR_NULL_TYPE, "the child node type cannot be null when creating an assignment");
        } else if(parentCtx.getID() == 0) {
            throw new PMException(Errors.ERR_NO_ID, "the parent node ID cannot be 0 when creating an assignment");
        } else if(parentCtx.getType() == null) {
            throw new PMException(Errors.ERR_NULL_TYPE, "the parent node type cannot be null when creating an assignment");
        } else if(!exists(childCtx.getID())) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("child node with ID %d does not exist", childCtx.getID()));
        } else if(!exists(parentCtx.getID())) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("parent node with ID %d does not exist", parentCtx.getID()));
        }

        //check if the assignment is valid
        Assignment.checkAssignment(childCtx.getType(), parentCtx.getType());

        //check the user can assign the child
        Decider decider = getDecider();
        if(!decider.hasPermissions(getUserID(), getProcessID(), childCtx.getID(), ASSIGN)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permission %s on node with ID %d", ASSIGN, childCtx.getID()));
        }

        //check that the user can assign to the parent
        // if the parent is a PC, check for permissions on the rep node
        long targetID = parentCtx.getID();
        if(parentCtx.getType().equals(PC)) {
            // get the policy class node
            NodeContext node = getGraphPAP().getNode(targetID);
            // get the rep property which is the ID of the rep node
            // set the target of the permission check to the rep node
            targetID = Long.parseLong(node.getProperties().get(REP_PROPERTY));
        }

        // check that the user can assign to the parent node
        if (!decider.hasPermissions(getUserID(), getProcessID(), targetID, ASSIGN_TO)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permission %s on node with ID %d", ASSIGN_TO, targetID));
        }

        getGraphPAP().assign(childCtx, parentCtx);
    }

    /**
     * Create the assignment in both the db and in-memory graphs. First check that the user is allowed to assign the child,
     * and allowed to assign something to the parent.
     * @param childCtx The context information for the child of the assignment to delete.
     * @param parentCtx The context information for the parent of the assignment to delete.
     * @throws PMException If the child node is null.
     * @throws PMException If the parent node is null.
     * @throws PMException If the child node does not have an ID or type.
     * @throws PMException If the parent node does not have an ID or type.
     * @throws PMException If the child node does not exist.
     * @throws PMException If the parent node does not exist.
     * @throws PMException If the current user does not have permission to delete the assignment.
     */
    @Override
    public void deassign(NodeContext childCtx, NodeContext parentCtx) throws PMException {
        // check that the parameters are correct
        if(childCtx == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "child node was null when deassigning");
        } else if(parentCtx == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "parent node was null when deassigning");
        } else if(childCtx.getID() == 0) {
            throw new PMException(Errors.ERR_NO_ID, "the child node ID cannot be 0 when deassigning");
        } else if(childCtx.getType() == null) {
            throw new PMException(Errors.ERR_NULL_TYPE, "the child node type cannot be null when deassigning");
        } else if(parentCtx.getID() == 0) {
            throw new PMException(Errors.ERR_NO_ID, "the parent node ID cannot be 0 when deassigning");
        } else if(parentCtx.getType() == null) {
            throw new PMException(Errors.ERR_NULL_TYPE, "the parent node type cannot be null when deassigning");
        } else if(!exists(childCtx.getID())) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("child node with ID %d could not be found when deassigning", childCtx.getID()));
        } else if(!exists(parentCtx.getID())) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("parent node with ID %d could not be found when deassigning", parentCtx.getID()));
        }

        Decider decider = getDecider();
        //check the user can deassign the child
        if(!decider.hasPermissions(getUserID(), getProcessID(), childCtx.getID(), DEASSIGN)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %s: %s", childCtx, DEASSIGN));
        }
        //check that the user can deassign from the parent
        // if the parent is a PC, check for permissions on the rep node
        long targetID = parentCtx.getID();
        if(parentCtx.getType().equals(PC)) {
            // get the policy class node
            NodeContext node = getGraphPAP().getNode(targetID);
            // get the rep property which is the ID of the rep node
            // set the target of the permission check to the rep node
            targetID = Long.parseLong(node.getProperties().get(REP_PROPERTY));
        }

        if (!decider.hasPermissions(getUserID(), getProcessID(), targetID, DEASSIGN_FROM)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %s: %s", targetID, DEASSIGN_FROM));
        }

        //delete assignment in PAP
        getGraphPAP().deassign(childCtx, parentCtx);
    }

    /**
     * Create an association between the User Attribute and the target node with the given operations. First, check that
     * the user has the permissions to associate the user attribute and target nodes.  If an association already exists
     * between the two nodes than update the existing association with the provided operations (overwrite).
     * @param uaCtx The context information of the User Attribute.
     * @param targetCtx The information for the target node.
     * @param operations A Set of operations to add to the Association.
     * @throws PMException If the user attribute node is null.
     * @throws PMException If the target node is null.
     * @throws PMException If the user attribute node does not have an ID or type.
     * @throws PMException If the target node does not have an ID or type.
     * @throws PMException If the user attribute node does not exist.
     * @throws PMException If the target node does not exist.
     * @throws PMException If the association is invalid.
     * @throws PMException If the current user does not have permission to create the association.
     */
    @Override
    public void associate(NodeContext uaCtx, NodeContext targetCtx, HashSet<String> operations) throws PMException {
        if(uaCtx == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "user attribute node was null");
        } else if(targetCtx == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "target node was null");
        } else if(uaCtx.getID() == 0) {
            throw new PMException(Errors.ERR_NO_ID, "the user attribute ID cannot be 0 when creating an association");
        } else if(targetCtx.getID() == 0) {
            throw new PMException(Errors.ERR_NO_ID, "the target node ID cannot be 0 when creating an association");
        } else if(targetCtx.getType() == null) {
            throw new PMException(Errors.ERR_NULL_TYPE, "the target node type cannot be null when creating an association");
        } else if(!exists(uaCtx.getID())) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node with ID %d could not be found when creating an association", uaCtx.getID()));
        } else if(!exists(targetCtx.getID())) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node with ID %d could not be found when creating an association", targetCtx.getID()));
        }

        NodeContext sourceNode = getNode(uaCtx.getID());
        NodeContext targetNode = getNode(targetCtx.getID());

        Association.checkAssociation(sourceNode.getType(), targetNode.getType());

        //check the user can associate the source and target nodes
        Decider decider = getDecider();
        if(!decider.hasPermissions(getUserID(), getProcessID(), uaCtx.getID(), ASSOCIATE)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %s: %s", uaCtx, ASSOCIATE));
        }
        if (!decider.hasPermissions(getUserID(), getProcessID(), targetCtx.getID(), ASSOCIATE)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %s: %s", targetCtx, ASSOCIATE));
        }

        //create association in PAP
        getGraphPAP().associate(uaCtx, targetCtx, operations);
    }

    /**
     * Delete the association between the user attribute and the target node.  First, check that the user has the
     * permission to delete the association.
     * @param uaCtx The ID of the User Attribute.
     * @param targetCtx The information for the target node.
     * @throws PMException If the user attribute node is null.
     * @throws PMException If the target node is null.
     * @throws PMException If the user attribute node does not have an ID.
     * @throws PMException If the target node does not have an ID.
     * @throws PMException If the user attribute node does not exist.
     * @throws PMException If the target node does not exist.
     * @throws PMException If the current user does not have permission to delete the association.
     */
    @Override
    public void dissociate(NodeContext uaCtx, NodeContext targetCtx) throws PMException {
        if(uaCtx == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "user attribute node was null");
        } else if(targetCtx == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "target node was null");
        } else if(uaCtx.getID() == 0) {
            throw new PMException(Errors.ERR_NO_ID, "the user attribute ID cannot be 0 when creating an association");
        } else if(targetCtx.getID() == 0) {
            throw new PMException(Errors.ERR_NO_ID, "the target node ID cannot be 0 when creating an association");
        } else if(!exists(uaCtx.getID())) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node with ID %d could not be found when creating an association", uaCtx.getID()));
        } else if(!exists(targetCtx.getID())) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node with ID %d could not be found when creating an association", targetCtx.getID()));
        }

        //check the user can associate the source and target nodes
        Decider decider = getDecider();
        if(!decider.hasPermissions(getUserID(), getProcessID(), uaCtx.getID(), DISASSOCIATE)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %s: %s", uaCtx, DISASSOCIATE));
        }
        if (!decider.hasPermissions(getUserID(), getProcessID(), targetCtx.getID(), DISASSOCIATE)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %s: %s", targetCtx, DISASSOCIATE));
        }

        //create association in PAP
        getGraphPAP().dissociate(uaCtx, targetCtx);
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
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node with ID %d could not be found", sourceID));
        }

        //check the user can get the associations of the source node
        Decider decider = getDecider();
        if(!decider.hasPermissions(getUserID(), getProcessID(), sourceID, GET_ASSOCIATIONS)){
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %s: %s", sourceID, GET_ASSOCIATIONS));
        }

        return getGraphPAP().getSourceAssociations(sourceID);
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
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node with ID %d could not be found", targetID));
        }

        //check the user can get the associations of the source node
        Decider decider = getDecider();
        if(!decider.hasPermissions(getUserID(), getProcessID(), targetID, GET_ASSOCIATIONS)){
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %s: %s", targetID, GET_ASSOCIATIONS));
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
     * @return A Response with the nodes that match the given search criteria.
     */
    @Override
    public HashSet<NodeContext> search(String name, String type, Map<String, String> properties) throws PMException {
        // user the PIP searcher to search for the intended nodes
        HashSet<NodeContext> nodes = getGraphPAP().search(name, type, properties);
        //filter out any nodes the user doesn't have any permissions on
        return getDecider().filter(getUserID(), getProcessID(), nodes);
    }

    /**
     * Retrieve the node from the graph with the given ID.
     *
     * @param id the ID of the node to get.
     * @return The Node retrieved from the graph with the given ID.
     *
     * @throws PMException If the node does not exist in the graph.
     * @throws PMException If the node is a Policy Class that doesn't have a rep node.
     */
    @Override
    public NodeContext getNode(long id) throws PMException {
        if(!exists(id)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node with ID %d could not be found", id));
        }

        NodeContext node = getGraphPAP().getNode(id);
        if(node.getType().equals(PC)) {
            id = Long.parseLong(node.getProperties().get(REP_PROPERTY));
            if(id == 0) {
                throw new PMException(Errors.ERR_NO_REP, String.format("the policy class with ID %d does not have a rep node", node.getID()));
            }
        }

        Decider decider = getDecider();
        if(!decider.hasPermissions(getUserID(), getProcessID(), id, ANY_OPERATIONS)) {
            throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("missing permissions on %s: %s", id, ANY_OPERATIONS));
        }

        return node;
    }
}
