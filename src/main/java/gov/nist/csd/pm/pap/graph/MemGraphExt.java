package gov.nist.csd.pm.pap.graph;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pap.loader.graph.GraphLoader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * MemGraphExt is an extension of the MemGraph implementation of the Graph interface.  This implementation also stores
 * the nodes in a separate data structure for quick retrieval of node information.
 */
public class MemGraphExt extends MemGraph {
    
    private HashMap<Long, Node> nodes;
    
    public MemGraphExt(GraphLoader graphLoader) throws DatabaseException {
        super(graphLoader);
        this.nodes = new HashMap<>();
    }

    /**
     * Getter for the underlying data structure containing all the nodes in the graph.
     * @return Map of all the nodes in the graph.
     */
    public HashMap<Long, Node> getNodesMap() {
        return nodes;
    }

    @Override
    public long createNode(Node node) throws NoIDException, NullNodeException {
        super.createNode(node);
        nodes.put(node.getID(), node);
        return node.getID();
    }

    @Override
    public void updateNode(Node node) {
        nodes.put(node.getID(), node);
    }

    @Override
    public void deleteNode(long nodeID) {
        super.deleteNode(nodeID);
        nodes.remove(nodeID);
    }

    @Override
    public boolean exists(long nodeID) {
        return super.exists(nodeID);
    }

    @Override
    public HashSet<Node> getNodes() {
        return new HashSet<>(nodes.values());
    }

    @Override
    public HashSet<Long> getPolicies() {
        return super.getPolicies();
    }

    @Override
    public HashSet<Node> getChildren(long nodeID) {
        HashSet<Node> retChildren = new HashSet<>();
        HashSet<Node> children = super.getChildren(nodeID);
        for(Node child : children) {
            retChildren.add(nodes.get(child.getID()));
        }
        return retChildren;
    }

    @Override
    public HashSet<Node> getParents(long nodeID) {
        HashSet<Node> retParents = new HashSet<>();
        HashSet<Node> parents = super.getParents(nodeID);
        for(Node parent : parents) {
            retParents.add(nodes.get(parent.getID()));
        }
        return retParents;
    }

    @Override
    public void assign(long childID, NodeType childType, long parentID, NodeType parentType) {
        super.assign(childID, childType, parentID, parentType);
    }

    @Override
    public void deassign(long childID, NodeType childType, long parentID, NodeType parentType) {
        super.deassign(childID, childType, parentID, parentType);
    }

    @Override
    public void associate(long uaID, long targetID, NodeType targetType, HashSet<String> operations) {
        super.associate(uaID, targetID, targetType, operations);
    }

    @Override
    public void dissociate(long uaID, long targetID, NodeType targetType) {
        super.dissociate(uaID, targetID, targetType);
    }

    @Override
    public HashMap<Long, HashSet<String>> getSourceAssociations(long sourceID) {
        return super.getSourceAssociations(sourceID);
    }

    @Override
    public HashMap<Long, HashSet<String>> getTargetAssociations(long targetID) {
        return super.getTargetAssociations(targetID);
    }
}
