package gov.nist.csd.pm.pap;

import gov.nist.csd.pm.common.exceptions.PMDBException;
import gov.nist.csd.pm.common.exceptions.PMGraphException;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.Graph;
import gov.nist.csd.pm.graph.MemGraph;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.nodes.NodeType;

import java.util.*;

public class GraphPAP implements Graph {

    private Graph dbGraph;
    private MemGraph memGraph;
    private HashMap<String, Long> namespaceNames;

    public GraphPAP(MemGraph memGraph, Graph dbGraph) throws PMException {
        this.memGraph = memGraph;
        this.dbGraph = dbGraph;
        this.namespaceNames = new HashMap<>();
    }

    @Override
    public Node createNode(long id, String name, NodeType nodeType, Map<String, String> properties) throws PMException {
        // check that the node namespace, name, and type do not already exist
        /*if(namespaceNames.get(nodeToNamespace(node)) != null) {
            throw new PMGraphException(String.format("a node with the name %s and type %s already exists in the name space %s",
                    node.getName(), node.getType(), node.getProperties().get(NAMESPACE_PROPERTY)));
        }*/

        Node node = dbGraph.createNode(id, name, nodeType, properties);
        memGraph.createNode(id, name, nodeType, properties);
        return node;
    }

    @Override
    public void updateNode(long id, String name, Map<String, String> properties) throws PMException {
        dbGraph.updateNode(id, name, properties);
        memGraph.updateNode(id, name, properties);
    }

    @Override
    public void deleteNode(long nodeID) throws PMException {
        dbGraph.deleteNode(nodeID);
        memGraph.deleteNode(nodeID);
    }

    @Override
    public boolean exists(long nodeID) {
        return memGraph.exists(nodeID);
    }

    @Override
    public Collection<Node> getNodes() {
        return memGraph.getNodes();
    }

    @Override
    public Set<Long> getPolicies() {
        return memGraph.getPolicies();
    }

    @Override
    public Set<Long> getChildren(long nodeID) throws PMException {
        return memGraph.getChildren(nodeID);
    }

    @Override
    public Set<Long> getParents(long nodeID) throws PMException {
        return memGraph.getParents(nodeID);
    }

    @Override
    public void assign(long childID, long parentID) throws PMException {
        dbGraph.assign(childID, parentID);
        memGraph.assign(childID, parentID);
    }

    @Override
    public void deassign(long childID, long parentID) throws PMException {
        dbGraph.deassign(childID, parentID);
        memGraph.deassign(childID, parentID);
    }

    @Override
    public void associate(long uaID, long targetID, Set<String> operations) throws PMException {
        dbGraph.associate(uaID, targetID, operations);
        memGraph.associate(uaID, targetID, operations);
    }

    @Override
    public void dissociate(long uaID, long targetID) throws PMException {
        dbGraph.dissociate(uaID, targetID);
        memGraph.dissociate(uaID, targetID);
    }

    @Override
    public Map<Long, Set<String>> getSourceAssociations(long sourceID) throws PMException {
        return memGraph.getSourceAssociations(sourceID);
    }

    @Override
    public Map<Long, Set<String>> getTargetAssociations(long targetID) throws PMException {
        return memGraph.getTargetAssociations(targetID);
    }

    @Override
    public Set<Node> search(String name, String type, Map<String, String> properties) {
        return memGraph.search(name, type, properties);
    }

    @Override
    public Node getNode(long id) throws PMException {
        return memGraph.getNode(id);
    }

    public void reset() throws PMException {
        Collection<Node> nodes = memGraph.getNodes();
        for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
            Node n = iterator.next();
            dbGraph.deleteNode(n.getID());
            iterator.remove();
        }
    }
}
