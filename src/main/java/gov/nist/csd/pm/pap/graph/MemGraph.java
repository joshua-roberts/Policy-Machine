package gov.nist.csd.pm.pap.graph;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.common.model.graph.relationships.Assignment;
import gov.nist.csd.pm.common.model.graph.relationships.Association;
import gov.nist.csd.pm.common.model.graph.relationships.Relationship;
import gov.nist.csd.pm.pap.loader.graph.GraphLoader;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DirectedMultigraph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gov.nist.csd.pm.common.constants.Properties.DEFAULT_NAMESPACE;
import static gov.nist.csd.pm.common.constants.Properties.NAMESPACE_PROPERTY;

/**
 * MemGraph is an in-memory implementation of the graph interface.  It stores the IDs of the nodes in a DAG structure.
 * And stores all other node information in a map for easy/fast retrieval.
 */
public class MemGraph implements Graph, Serializable {

    private DirectedGraph<Long, Relationship> graph;
    private HashSet<Long>                     pcs;
    private HashMap<Long, NodeContext>        nodes;
    private HashMap<String, Long>             namespaceNames;

    public MemGraph(GraphLoader graphLoader) throws PMException {
        graph = new DirectedMultigraph<>(Relationship.class);
        nodes = new HashMap<>();
        namespaceNames = new HashMap<>();

        //load the policies
        pcs = graphLoader.getPolicies();

        //load the graph using the graphLoader
        //load the nodes
        HashSet<NodeContext> nodes = graphLoader.getNodes();
        for(NodeContext node : nodes) {
            this.createNode(node);
        }

        //load the assignments
        HashSet<Assignment> assignments = graphLoader.getAssignments();
        for(Assignment assignment : assignments) {
            graph.addEdge(assignment.getSourceID(), assignment.getTargetID(), assignment);
        }

        //load the associations
        HashSet<Association> associations = graphLoader.getAssociations();
        for(Association association : associations) {
            graph.addEdge(association.getSourceID(), association.getTargetID(), association);
        }
    }

    /**
     * Default constructor to create an empty graph in memory.
     */
    public MemGraph() {
        graph = new DirectedMultigraph<>(Relationship.class);
        nodes = new HashMap<>();
        namespaceNames = new HashMap<>();
        pcs = new HashSet<>();
    }

    /**
     * Getter for the underlying data structure containing all the nodes in the graph.
     * @return Map of all the nodes in the graph.
     */
    public HashMap<Long, NodeContext> getNodesMap() {
        return nodes;
    }

    public HashMap<String, Long> getNamespaceNames() {
        return namespaceNames;
    }

    /**
     * Create a node in the in-memory graph.  The ID field of the passed Node must not be 0.
     * @param node The context of the node to create.  This includes the id, name, type, and properties.
     * @return The ID that was passed as part of the node parameter.
     * @throws PMException When the provided node is null.
     * @throws PMException When the provided node has an ID of 0.
     * @throws PMException When the provided node has a null or empty name.
     * @throws PMException When the provided node has a null type.
     */
    @Override
    public long createNode(NodeContext node) throws PMException {
        //check for null values
        if(node == null) {
            throw new PMException(Errors.ERR_NULL_NODE_CTX, "a null node was provided when creating a node in-memory");
        } else if(node.getID() == 0) {
            throw new PMException(Errors.ERR_NO_ID, "no ID was provided when creating a node in the in-memory graph");
        } else if(node.getName() == null || node.getName().isEmpty()) {
            throw new PMException(Errors.ERR_NULL_NAME, "no name was provided when creating a node in the in-memory graph");
        } else if(node.getType() == null) {
            throw new PMException(Errors.ERR_NULL_TYPE, "a null type was provided to the in memory graph when creating a node");
        }

        //if the node being created is a PC, add it to the graph and list of policies
        if (node.getType().equals(NodeType.PC)) {
            pcs.add(node.getID());
            graph.addVertex(node.getID());
        } else {
            graph.addVertex(node.getID());
        }

        // store the namespace:name -> id
        addNamespaceName(node);

        //store the node in the map
        nodes.put(node.getID(), new NodeContext(node.getID(), node.getName(), node.getType(), node.getProperties()));

        //return the Node with the given info about the node
        return node.getID();
    }

    private void addNamespaceName(NodeContext node) {
        String namespace = node.getProperties().get(NAMESPACE_PROPERTY);
        String name = node.getName();
        NodeType type = node.getType();
        long id = node.getID();
        if(namespace == null) {
            namespace = DEFAULT_NAMESPACE;
        }
        this.namespaceNames.put(namespace + ":" + name + ":" + type, id);
    }

    private void removeNamespaceName(String namespace, String name, String type) {
        if(namespace == null) {
            namespace = "";
        }
        this.namespaceNames.remove(namespace + ":" + name + ":" + type);
    }

    @Override
    public void updateNode(NodeContext node) {
        nodes.put(node.getID(), node);
    }

    @Override
    public void deleteNode(long nodeID) {
        NodeContext node = nodes.get(nodeID);
        //remove the vertex from the graph
        graph.removeVertex(nodeID);
        //remove the node from the policies if it is a policy class
        pcs.remove(nodeID);
        //remove the node from the map
        nodes.remove(nodeID);
        //remove name from namespace
        removeNamespaceName(node.getProperties().get(NAMESPACE_PROPERTY), node.getName(), node.getType().toString());
    }

    @Override
    public boolean exists(long nodeID) {
        return graph.containsVertex(nodeID);
    }

    @Override
    public HashSet<NodeContext> getNodes() {
        return new HashSet<>(nodes.values());
    }

    @Override
    public HashSet<Long> getPolicies() {
        return pcs;
    }

    /**
     * Find all the nodes that are assigned to the given node.
     * @param nodeID The ID of the node to get the children of.
     * @return The set of nodes that are assigned to the given node.  The returned set will include each node's information provided in NodeContext objects.
     * @throws PMException If the provided nodeID does not exist in the graph.
     */
    @Override
    public HashSet<NodeContext> getChildren(long nodeID) throws PMException {
        if(!exists(nodeID)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node %s does not exist in the graph", nodeID));
        }

        HashSet<NodeContext> children = new HashSet<>();
        Set<Relationship> rels = graph.incomingEdgesOf(nodeID);
        for(Relationship rel : rels){
            if(rel instanceof Association) {
                continue;
            }
            children.add(nodes.get(rel.getSourceID()));
        }
        return children;
    }

    /**
     * Find all the nodes that the given node is assigned to.
     * @param nodeID The ID of the node to get the parents of.
     * @return The set of nodes the given node is assigned to.  The returned set will include each node's information provided in NodeContext objects.
     * @throws PMException If the provided nodeID does not exist in the graph.
     */
    @Override
    public HashSet<NodeContext> getParents(long nodeID) throws PMException {
        if(!exists(nodeID)) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node %s does not exist in the graph", nodeID));
        }

        HashSet<NodeContext> parents = new HashSet<>();
        Set<Relationship> rels = graph.outgoingEdgesOf(nodeID);
        for(Relationship rel : rels){
            if(rel instanceof Association) {
                continue;
            }
            parents.add(nodes.get(rel.getTargetID()));
        }
        return parents;
    }

    /**
     * Assign the child node to the parent node.
     *
     * @param childCtx The context information for the child in the assignment.  The ID and type are required.
     * @param parentCtx The context information for the parent in the assignment The ID and type are required.
     * @throws PMException If the child node does not exist in the graph.
     * @throws PMException If the parent node does not exist in the graph.
     */
    @Override
    public void assign(NodeContext childCtx, NodeContext parentCtx) throws PMException {
        if(!exists(childCtx.getID())) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node %s does not exist in the graph", childCtx));
        } else if(!exists(parentCtx.getID())) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node %s does not exist in the graph", parentCtx));
        }

        graph.addEdge(childCtx.getID(), parentCtx.getID(), new Assignment(childCtx.getID(), parentCtx.getID()));
    }

    @Override
    public void deassign(NodeContext childCtx, NodeContext parentCtx) {
        graph.removeEdge(childCtx.getID(), parentCtx.getID());
    }

    /**
     * Associate the User Attribute node and the target node.
     *
     * @param uaCtx The information for the User Attribute in the association.
     * @param targetCtx The context information for the target of the association.
     * @param operations A Set of operations to add to the association.
     * @throws PMException If the User Attribute node does not exist in the graph.
     * @throws PMException If the target node does not exist in the graph.
     */
    @Override
    public void associate(NodeContext uaCtx, NodeContext targetCtx, HashSet<String> operations) throws PMException {
        if(!exists(uaCtx.getID())) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node %s does not exist in the graph", uaCtx.getID()));
        } else if(!exists(targetCtx.getID())) {
            throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node %s does not exist in the graph", targetCtx.getID()));
        }

        if(graph.containsEdge(uaCtx.getID(), targetCtx.getID())) {
            // if the association exists update the operations
            Association assoc = (Association) graph.getEdge(uaCtx.getID(), targetCtx.getID());
            assoc.setOperations(operations);
        } else {
            graph.addEdge(uaCtx.getID(), targetCtx.getID(), new Association(uaCtx.getID(), targetCtx.getID(), operations));
        }
    }

    @Override
    public void dissociate(NodeContext uaCtx, NodeContext targetCtx) {
        graph.removeEdge(uaCtx.getID(), targetCtx.getID());
    }

    @Override
    public HashMap<Long, HashSet<String>> getSourceAssociations(long sourceID) {
        HashMap<Long, HashSet<String>> assocs = new HashMap<>();
        Set<Relationship> rels = graph.outgoingEdgesOf(sourceID);
        for(Relationship rel : rels){
            if(rel instanceof Association){
                Association assoc = (Association) rel;
                assocs.put(assoc.getTargetID(), assoc.getOperations());
            }
        }
        return assocs;
    }

    @Override
    public HashMap<Long, HashSet<String>> getTargetAssociations(long targetID) {
        HashMap<Long, HashSet<String>> assocs = new HashMap<>();
        Set<Relationship> rels = graph.incomingEdgesOf(targetID);
        for(Relationship rel : rels){
            if(rel instanceof Association){
                Association assoc = (Association) rel;
                assocs.put(assoc.getSourceID(), assoc.getOperations());
            }
        }
        return assocs;
    }
}
