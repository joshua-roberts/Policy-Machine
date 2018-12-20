package gov.nist.csd.pm.common.model.graph;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;

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
     */
    long createNode(Node node) throws PMException;
    /**
     * Update the name and properties of the node with the given ID. The node's existing properties will be overwritten 
     * by the ones provided. The name parameter is optional and will be ignored if null or empty.  The properties 
     * parameter will be ignored only if null.  If the map is empty, the node's properties will be overwritten
     * with the empty map.
     * @param node The node to update. This includes the id, name, and properties.
     */
    void updateNode(Node node) throws PMException;
    /**
     * Delete the node with the given ID from the graph.
     * @param nodeID the ID of the node to delete.
     */
    void deleteNode(long nodeID) throws PMException;

    /**
     * Check that a node with the given ID exists in the graph.
     * @param nodeID the ID of the node to check for.
     * @return True or False if a node with the given ID exists or not.
     */
    boolean exists(long nodeID) throws PMException;

    /**
     * Retrieve the set of all nodes in the graph.
     * @return A Set of all the nodes in the graph.
     */
    HashSet<Node> getNodes() throws PMException;

    /**
     * Get the set of policy classes.  This operation is run every time a decision is made, so a separate
     * method is needed to improve efficiency. The returned set is just the IDs of each policy class.
     * @return The set of policy class IDs.
     */
    HashSet<Long> getPolicies() throws PMException;

    /**
     * Get the set of nodes that are assigned to the node with the given ID.
     * @param nodeID The ID of the node to get the children of.
     * @return The Set of NGACNodes that are assigned to the node with the given ID.
     */
    HashSet<Node> getChildren(long nodeID) throws PMException;

    /**
     * Get the set of nodes that the node with the given ID is assigned to.
     * @param nodeID The ID of the node to get the parents of.
     * @return The Set of NGACNodes that are assigned to the node with the given ID.
     */
    HashSet<Node> getParents(long nodeID) throws PMException;

    /**
     * Assign the child node to the parent node. The child and parent nodes must both already exist in the graph,
     * and the types must make a valid assignment.
     * @param childID The ID of the child node.
     * @param childType The type of the child node.
     * @param parentID The the ID of the parent node.
     * @param parentType The type of the parent node.
     */
    void assign(long childID, NodeType childType, long parentID, NodeType parentType) throws PMException;

    /**
     * Remove the Assignment between the child and parent nodes.
     * @param childID The ID of the child node.
     * @param childType The type of the child node.
     * @param parentID The the ID of the parent node.
     * @param parentType The type of the parent node.
     */
    void deassign(long childID, NodeType childType, long parentID, NodeType parentType) throws PMException;

    /**
     * Create an Association between the User Attribute and the Target node with the provided operations. If an association
     * already exists between these two nodes, overwrite the existing operations with the ones provided.
     *
     * @param uaID The ID of the user Attribute.
     * @param targetID The ID of the target node.
     * @param targetType The type of the target node.
     * @param operations A Set of operations to add to the Association.
     */
    void associate(long uaID, long targetID, NodeType targetType, HashSet<String> operations) throws PMException;

    /**
     * Delete the Association between the User Attribute and Target node.
     * @param uaID The ID of the User Attribute.
     * @param targetID The ID of the target node.
     * @param targetType The type of the target node.
     */
    void dissociate(long uaID, long targetID, NodeType targetType) throws PMException;

    /**
     * Retrieve the associations the given node is the source of.  The source node of an association is always a
     * User Attribute and this method will throw an exception if an invalid node is provided.  The returned Map will
     * contain the target and operations of each association.
     * @param sourceID The ID of the source node.
     * @return A Map of the target node IDs and the operations for each association.
     */
    HashMap<Long, HashSet<String>> getSourceAssociations(long sourceID) throws PMException;

    /**
     * Retrieve the associations the given node is the target of.  The target node can be an Object Attribute or a User
     * Attribute. This method will throw an exception if a node of any other type is provided.  The returned Map will
     * contain the source node IDs and the operations of each association.
     * @param targetID the ID of the target node.
     * @return A Map of the source Ids and the operations for each association.
     */
    HashMap<Long, HashSet<String>> getTargetAssociations(long targetID) throws PMException;
}
