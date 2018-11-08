package gov.nist.csd.pm.pap.graph;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Graph;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.model.graph.relationships.NGACAssignment;
import gov.nist.csd.pm.model.graph.relationships.NGACAssociation;
import gov.nist.csd.pm.model.graph.relationships.NGACRelationship;
import gov.nist.csd.pm.pap.loader.graph.GraphLoader;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DirectedMultigraph;
import java.util.*;

/**
 * This is a minimalist approach to the NGAC graph in which only node IDs are stored. The only type that is stored
 * separately are Policy Classes, which are stored in the graph as well as a separate data structure for quick access.
 * Retrieving node information will be done through a database.
 */
public class MemGraph implements Graph {
    private DirectedGraph<Long, NGACRelationship> graph;
    private HashSet<Long> pcs;

    /**
     * Create a new in-memory graph.
     * @param graphLoader The loader to load a graph from.
     * @throws DatabaseException If an error occurs when loading the graph from the database
     */
    public MemGraph(GraphLoader graphLoader) throws DatabaseException {
        graph = new DirectedMultigraph<>(NGACRelationship.class);

        //load the graph using the graphLoader
        //load the nodes
        HashSet<Long> nodes = graphLoader.getNodes();
        for(long node : nodes) {
            graph.addVertex(node);
        }

        //load the assignments
        HashSet<NGACAssignment> assignments = graphLoader.getAssignments();
        for(NGACAssignment assignment : assignments) {
            graph.addEdge(assignment.getSourceID(), assignment.getTargetID(), assignment);
        }

        //load the associations
        HashSet<NGACAssociation> associations = graphLoader.getAssociations();
        for(NGACAssociation association : associations) {
            graph.addEdge(association.getSourceID(), association.getTargetID(), association);
        }

        //load the policies
        pcs = graphLoader.getPolicies();
    }

    /**
     * Add a new ID to the graph.
     * @param id The ID of the node to add.
     */
    private void addNode(long id) {
        graph.addVertex(id);
    }

    /**
     * Add a Policy Class to the list of policies and the graph.
     * @param id The ID of the Policy Class to add.
     */
    private void addPolicy(long id) {
        pcs.add(id);
        graph.addVertex(id);
    }

    /**
     * Create a new node and add it to the graph. Since this implementation relies on the ID of the nodes,
     * the ID of the context needs to be set to a non zero value.  An exception will be thrown if the ID is zero.
     * @param node The context of the node to create.  This includes the id, name, type, and properties.
     *            The name and properties will be ignored in this implementation.
     * @return An empty Node since only IDs are stored in this implementation.
     * @throws PMException When the node context is null, the ID is zero, or the type is null.
     */
    @Override
    public long createNode(Node node) throws NoIDException, NullNodeException {
        //check for null values
        if(node == null) {
            throw new NullNodeException();
        } else if(node.getID() == 0) {
            throw new NoIDException();
        }

        //if the node being created is a PC, add it to the graph and list of policies
        if (node.getType().equals(NodeType.PC)) {
            addPolicy(node.getID());
        } else {
            addNode(node.getID());
        }

        //return the Node with the given info about the node
        return node.getID();
    }

    /**
     * Since this implementation only focuses on IDs and types, a node's name and properties in this graph
     * can not be updated.
     */
    @Override
    public void updateNode(Node node) { }

    /**
     * Delete the node from the graph. If it's a policy class node then delete it from the list of policies too.
     * @param nodeID the ID of the node to delete.
     */
    @Override
    public void deleteNode(long nodeID) {
        //remove the vertex from the graph
        graph.removeVertex(nodeID);
        //remove the node from the policies if it is a policy class
        pcs.remove(nodeID);
    }

    @Override
    public boolean exists(long nodeID) {
        return graph.containsVertex(nodeID);
    }

    /**
     * Retrieve the set of all nodes in the graph. Each element in the set will be an Node but will only contain the
     * ID of the node, no further information is stored in this implementation.
     * @return The set of all node IDs in the graph.
     */
    @Override
    public HashSet<Node> getNodes() {
        HashSet<Node> nodes = new HashSet<>();
        Set<Long> vertexSet = graph.vertexSet();
        for(Long id : vertexSet) {
            nodes.add(new Node().id(id));
        }

        return nodes;
    }

    @Override
    public HashSet<Long> getPolicies() {
        return pcs;
    }

    /**
     * Get the children of a node.  The children are the nodes that are assigned to the given node.
     * The returned set will only contain the IDs of the child nodes.
     * @param nodeID The ID of the node to get the children of.
     * @return A HashSet containing the IDs of the nodes assigned to the given node.
     */
    @Override
    public HashSet<Node> getChildren(long nodeID) {
        HashSet<Node> children = new HashSet<>();
        Set<NGACRelationship> rels = graph.incomingEdgesOf(nodeID);
        for(NGACRelationship rel : rels){
            if(rel instanceof NGACAssociation) {
                continue;
            }
            children.add(new Node().id(rel.getSourceID()));
        }
        return children;
    }

    /**
     * Get the parents of a node.  The parents are the nodes that the given node is assigned to.
     * The returned set will only contain the IDs of the child nodes.
     * @param nodeID The ID of the node to get the parents of.
     * @return A HashSet containing the IDs of the nodes the given node is assigned to.
     */
    @Override
    public HashSet<Node> getParents(long nodeID) {
        HashSet<Node> parents = new HashSet<>();
        Set<NGACRelationship> rels = graph.outgoingEdgesOf(nodeID);
        for(NGACRelationship rel : rels){
            if(rel instanceof NGACAssociation) {
                continue;
            }
            parents.add(new Node().id(rel.getTargetID()));
        }
        return parents;
    }

    @Override
    public void assign(long childID, NodeType childType, long parentID, NodeType parentType) {
        graph.addEdge(childID, parentID, new NGACAssignment(childID, parentID));
    }

    @Override
    public void deassign(long childID, NodeType childType, long parentID, NodeType parentType) {
        graph.removeEdge(childID, parentID);
    }

    @Override
    public void associate(long uaID, long targetID, NodeType targetType, HashSet<String> operations) {
        if(graph.containsEdge(uaID, targetID)) {
            // if the association exists update the operations
            NGACAssociation assoc = (NGACAssociation) graph.getEdge(uaID, targetID);
            assoc.setOperations(operations);
        } else {
            graph.addEdge(uaID, targetID, new NGACAssociation(uaID, targetID, operations));
        }
    }

    @Override
    public void dissociate(long uaID, long targetID, NodeType targetType) {
        graph.removeEdge(uaID, targetID);
    }

    @Override
    public HashMap<Long, HashSet<String>> getSourceAssociations(long sourceID) {
        HashMap<Long, HashSet<String>> assocs = new HashMap<>();
        Set<NGACRelationship> rels = graph.outgoingEdgesOf(sourceID);
        for(NGACRelationship rel : rels){
            if(rel instanceof NGACAssociation){
                NGACAssociation assoc = (NGACAssociation) rel;
                assocs.put(assoc.getTargetID(), assoc.getOperations());
            }
        }
        return assocs;
    }

    @Override
    public HashMap<Long, HashSet<String>> getTargetAssociations(long targetID) {
        HashMap<Long, HashSet<String>> assocs = new HashMap<>();
        Set<NGACRelationship> rels = graph.incomingEdgesOf(targetID);
        for(NGACRelationship rel : rels){
            if(rel instanceof NGACAssociation){
                NGACAssociation assoc = (NGACAssociation) rel;
                assocs.put(assoc.getSourceID(), assoc.getOperations());
            }
        }
        return assocs;
    }
}
