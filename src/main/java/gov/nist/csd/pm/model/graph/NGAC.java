package gov.nist.csd.pm.model.graph;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.pip.loader.LoaderException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public interface NGAC {
    /**
     * Create a new node with the given name, type and properties and add it to the graph.
     * @param ctx The context of the node to create.  This includes the id, name, type, and properties.
     * @return A OldNode object with it's ID.
     */
    Node createNode(Node ctx) throws NoIDException, NullTypeException, NullNameException, NullNodeCtxException, DatabaseException, LoadConfigException, LoaderException;

    /**
     * Update the name and properties of the node with the given ID. The node's existing properties will be overwritten 
     * by the ones provided. The name parameter is optional and will be ignored if null or empty.  The properties 
     * parameter will be ignored only if null.  If the map is empty, the node's properties will be overwritten
     * with the empty map.
     * @param ctx The context of the node to update. This includes the id, name, type, and properties.
     */
    void updateNode(Node ctx) throws NullNodeCtxException, NoIDException, DatabaseException, LoadConfigException, LoaderException;

    /**
     * Delete the node with the given ID from the graph.
     * @param nodeID the ID of the node to delete.
     */
    void deleteNode(long nodeID) throws DatabaseException, LoaderException, SessionDoesNotExistException, LoadConfigException, MissingPermissionException;

    /**
     * Check that a node with the given ID exists in the graph.
     * @param nodeID the ID of the node to check for.
     * @return True or False if a node with the given ID exists or not.
     */
    boolean exists(long nodeID) throws DatabaseException, LoadConfigException, LoaderException;

    /**
     * Retrieve the set of all nodes in the graph.
     * @return A Set of all the nodes in the graph.
     */
    HashSet<Node> getNodes() throws DatabaseException, LoadConfigException, LoaderException;

    /**
     * Get the set of policy classes.  This operation is run every time a decision is made, so a separate
     * method is needed to improve efficiency. The returned set is just the IDs of each policy class.
     * @return The set of policy class IDs.
     */
    HashSet<Long> getPolicies() throws DatabaseException, LoadConfigException, LoaderException;

    /**
     * Get the set of nodes that are assigned to the node with the given ID.
     * @param nodeID The ID of the node to get the children of.
     * @return The Set of NGACNodes that are assigned to the node with the given ID.
     */
    HashSet<Node> getChildren(long nodeID) throws DatabaseException, LoaderException, SessionDoesNotExistException, LoadConfigException;

    /**
     * Get the set of nodes that the node with the given ID is assigned to.
     * @param nodeID The ID of the node to get the parents of.
     * @return The Set of NGACNodes that are assigned to the node with the given ID.
     */
    HashSet<Node> getParents(long nodeID) throws DatabaseException, LoadConfigException, LoaderException, SessionDoesNotExistException;

    /**
     * Assign the child node to the parent node.
     * @param childCtx The ID and type of the child in this assignment
     * @param parentCtx The ID and type of the parent in this assignment
     */
    void assign(Node childCtx, Node parentCtx) throws DatabaseException, NullNodeCtxException, NullTypeException, LoaderException, SessionDoesNotExistException, LoadConfigException, MissingPermissionException;

    /**
     * Remove the Assignment between the child and parent nodes.
     * @param childCtx The ID and type of the child in this assignment
     * @param parentCtx The ID and type of the parent in this assignment    
     */
    void deassign(Node childCtx, Node parentCtx) throws DatabaseException, NullNodeCtxException, NullTypeException, LoaderException, SessionDoesNotExistException, LoadConfigException, MissingPermissionException;

    /**
     * Create an Association between the User Attribute and the Target node with the provided operations. If an association
     * already exists between these two nodes, overwrite the existing operations with the ones provided.
     * @param uaID The ID of the User Attribute.
     * @param targetID The ID of the target node.
     * @param operations A Set of operations to add to the Association.
     */
    void associate(long uaID, long targetID, Collection<String> operations) throws DatabaseException, LoadConfigException, LoaderException, MissingPermissionException, SessionDoesNotExistException;

    /**
     * Delete the Association between the User Attribute and Target node.
     * @param uaID The ID of the User Attribute.
     * @param targetID The ID of the target node.
     */
    void dissociate(long uaID, long targetID) throws DatabaseException, LoaderException, SessionDoesNotExistException, LoadConfigException, MissingPermissionException;

    /**
     * Retrieve the associations the given node is the source of.  The source node of an association is always a
     * User Attribute and this method will throw an exception if an invalid node is provided.  The returned Map will
     * contain the target and operations of each association.
     * @param sourceID The ID of the source node.
     * @return A Map of the target node IDs and the operations for each association.
     */
    HashMap<Long, Collection<String>> getSourceAssociations(long sourceID) throws DatabaseException, LoaderException, SessionDoesNotExistException, LoadConfigException, MissingPermissionException;

    /**
     * Retrieve the associations the given node is the target of.  The target node can be an Object Attribute or a User
     * Attribute. This method will throw an exception if a node of any other type is provided.  The returned Map will
     * contain the source node IDs and the operations of each association.
     * @param targetID the ID of the target node.
     * @return A Map of the source Ids and the operations for each association.
     */
    HashMap<Long, Collection<String>> getTargetAssociations(long targetID) throws DatabaseException, LoaderException, SessionDoesNotExistException, LoadConfigException, MissingPermissionException;
}
