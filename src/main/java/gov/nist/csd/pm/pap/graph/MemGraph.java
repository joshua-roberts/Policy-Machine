package gov.nist.csd.pm.pap.graph;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.common.model.graph.relationships.NGACAssignment;
import gov.nist.csd.pm.common.model.graph.relationships.NGACAssociation;
import gov.nist.csd.pm.common.model.graph.relationships.NGACRelationship;
import gov.nist.csd.pm.pap.loader.graph.GraphLoader;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * MemGraphExt is an extension of the MemGraph implementation of the Graph interface.  This implementation also stores
 * the nodes in a separate data structure for quick retrieval of node information.
 */
public class MemGraph implements Graph {

    private DirectedGraph<Long, NGACRelationship> graph;
    private HashSet<Long>                         pcs;
    private HashMap<Long, Node>                   nodes;
    
    public MemGraph(GraphLoader graphLoader) throws PMException {
        graph = new DirectedMultigraph<>(NGACRelationship.class);
        nodes = new HashMap<>();

        //load the graph using the graphLoader
        //load the nodes
        HashSet<Node> nodes = graphLoader.getNodes();
        for(Node node : nodes) {
            graph.addVertex(node.getID());
            this.nodes.put(node.getID(), node);
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
     * Getter for the underlying data structure containing all the nodes in the graph.
     * @return Map of all the nodes in the graph.
     */
    public HashMap<Long, Node> getNodesMap() {
        return nodes;
    }

    @Override
    public long createNode(Node node) throws PMException {
        //check for null values
        if(node == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "a null node was provided when creating a node in-memory");
        } else if(node.getID() == 0) {
            throw new PMException(Errors.ERR_NO_ID, "no ID was provided when creating a node in the in-memory graph");
        }

        //if the node being created is a PC, add it to the graph and list of policies
        if (node.getType().equals(NodeType.PC)) {
            pcs.add(node.getID());
            graph.addVertex(node.getID());
        } else {
            graph.addVertex(node.getID());
        }

        //store all of the node information
        nodes.put(node.getID(), node);

        //return the Node with the given info about the node
        return node.getID();
    }

    @Override
    public void updateNode(Node node) {
        nodes.put(node.getID(), node);
    }

    @Override
    public void deleteNode(long nodeID) {
        //remove the vertex from the graph
        graph.removeVertex(nodeID);
        //remove the node from the policies if it is a policy class
        pcs.remove(nodeID);
        //remove the node from the map
        nodes.remove(nodeID);
    }

    @Override
    public boolean exists(long nodeID) {
        return graph.containsVertex(nodeID);
    }

    @Override
    public HashSet<Node> getNodes() {
        return new HashSet<>(nodes.values());
    }

    @Override
    public HashSet<Long> getPolicies() {
        return pcs;
    }

    @Override
    public HashSet<Node> getChildren(long nodeID) {
        HashSet<Node> children = new HashSet<>();
        Set<NGACRelationship> rels = graph.incomingEdgesOf(nodeID);
        for(NGACRelationship rel : rels){
            if(rel instanceof NGACAssociation) {
                continue;
            }
            children.add(nodes.get(rel.getSourceID()));
        }
        return children;
    }

    @Override
    public HashSet<Node> getParents(long nodeID) {
        HashSet<Node> parents = new HashSet<>();
        Set<NGACRelationship> rels = graph.outgoingEdgesOf(nodeID);
        for(NGACRelationship rel : rels){
            if(rel instanceof NGACAssociation) {
                continue;
            }
            parents.add(nodes.get(rel.getTargetID()));
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
