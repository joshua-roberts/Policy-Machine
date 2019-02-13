package gov.nist.csd.pm.common.model.graph;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pep.requests.CreateNodeRequest;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Interface for maintaining an NGAC graph.
 */
public interface Graph {

    /**
     * Create a new node with the given name, type and properties and add it to the graph.
     * @param node The context of the node to create.  This includes the id, name, type, and properties.
     * @return The ID of the newly created node.
     * @throws PMException If there is an error creating the node.
     */
    long createNode(NodeContext node) throws PMException;

    /**
     * Update the name and properties of the node with the given ID. The node's existing properties will be overwritten 
     * by the ones provided. The name parameter is optional and will be ignored if null or empty.  The properties 
     * parameter will be ignored only if null.  If the map is empty, the node's properties will be overwritten
     * with the empty map.
     * @param node The node to update. This includes the id, name, and properties.
     * @throws PMException If there is an error updating the node.
     */
    void updateNode(NodeContext node) throws PMException;

    /**
     * Delete the node with the given ID from the graph.
     * @param nodeID the ID of the node to delete.
     * @throws PMException If there is an error deleting the node.
     */
    void deleteNode(long nodeID) throws PMException;

    /**
     * Check that a node with the given ID exists in the graph.
     * @param nodeID the ID of the node to check for.
     * @return True or False if a node with the given ID exists or not.
     * @throws PMException If there is an error checking if the node exists.
     */
    boolean exists(long nodeID) throws PMException;

    /**
     * Retrieve the set of all nodes in the graph.
     * @return A Set of all the nodes in the graph.
     * @throws PMException If there is an error retrieving node.
     */
    HashSet<NodeContext> getNodes() throws PMException;

    /**
     * Get the set of policy classes.  This operation is run every time a decision is made, so a separate
     * method is needed to improve efficiency. The returned set is just the IDs of each policy class.
     * @return The set of policy class IDs.
     * @throws PMException If there is an error retrieving the IDs of the Policy Classes.
     */
    HashSet<Long> getPolicies() throws PMException;

    /**
     * Get the set of nodes that are assigned to the node with the given ID.
     * @param nodeID The ID of the node to get the children of.
     * @return The Set of NGACNodes that are assigned to the node with the given ID.
     * @throws PMException If there is an error getting the children of the given node.
     */
    HashSet<NodeContext> getChildren(long nodeID) throws PMException;

    /**
     * Get the set of nodes that the node with the given ID is assigned to.
     * @param nodeID The ID of the node to get the parents of.
     * @return The Set of NGACNodes that are assigned to the node with the given ID.
     * @throws PMException If there is an error getting the parents of the given node.
     */
    HashSet<NodeContext> getParents(long nodeID) throws PMException;

    /**
     * Assign the child node to the parent node. The child and parent nodes must both already exist in the graph,
     * and the types must make a valid assignment. An example of a valid assignment is assigning o1, an object, to oa1,
     * an object attribute.  o1 is the child (objects can never be the parent in an assignment), and oa1 is the parent.
     *
     * @param childCtx The context information for the child in the assignment.  The ID and type are required.
     * @param parentCtx The context information for the parent in the assignment The ID and type are required.
     * @throws PMException If there is an error assigning the child node to the parent node.
     */
    void assign(NodeContext childCtx, NodeContext parentCtx) throws PMException;

    /**
     * Remove the Assignment between the child and parent nodes.
     * @param childCtx The context information for the child of the assignment.
     * @param parentCtx The context information for the parent of the assignment.
     * @throws PMException If there is an error deassigning the child node from the parent node.
     */
    void deassign(NodeContext childCtx, NodeContext parentCtx) throws PMException;

    /**
     * Create an Association between the User Attribute and the Target node with the provided operations. If an association
     * already exists between these two nodes, overwrite the existing operations with the ones provided.  Associations
     * can only begin at a User Attribute but can point to either an Object or User Attribute
     *
     * @param uaCtx The information for the User Attribute in the association.
     * @param targetCtx The context information for the target of the association.
     * @param operations A Set of operations to add to the association.
     *
     * @throws PMException If there is an error associating the two nodes.
     */
    void associate(NodeContext uaCtx, NodeContext targetCtx, HashSet<String> operations) throws PMException;

    /**
     * Delete the Association between the User Attribute and Target node.
     * @param uaCtx The context information for the User Attribute of the association.
     * @param targetCtx The context information for the target of the association.
     *
     * @throws PMException If there is an error dissociating the User Attribute node and the target node.
     */
    void dissociate(NodeContext uaCtx, NodeContext targetCtx) throws PMException;

    /**
     * Retrieve the associations the given node is the source of.  The source node of an association is always a
     * User Attribute and this method will throw an exception if an invalid node is provided.  The returned Map will
     * contain the target and operations of each association.
     * @param sourceID The ID of the source node.
     * @return A Map of the target node IDs and the operations for each association.
     * @throws PMException If there is an exception getting the associations for the source node.
     */
    HashMap<Long, HashSet<String>> getSourceAssociations(long sourceID) throws PMException;

    /**
     * Retrieve the associations the given node is the target of.  The target node can be an Object Attribute or a User
     * Attribute. This method will throw an exception if a node of any other type is provided.  The returned Map will
     * contain the source node IDs and the operations of each association.
     * @param targetID the ID of the target node.
     * @return A Map of the source Ids and the operations for each association.
     * @throws PMException If there is an error getting the associations for the target node.
     */
    HashMap<Long, HashSet<String>> getTargetAssociations(long targetID) throws PMException;
}
