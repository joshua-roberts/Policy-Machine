package gov.nist.csd.pm.pap.graph;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.pap.db.DatabaseContext;
import gov.nist.csd.pm.pap.loader.graph.GraphLoader;
import gov.nist.csd.pm.pap.loader.graph.Neo4jGraphLoader;
import gov.nist.csd.pm.pap.search.MemGraphSearch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GraphPAP implements Graph, Search {

    private Graph dbGraph;
    private MemGraph memGraph;

    public GraphPAP(DatabaseContext ctx) throws PMException {
        dbGraph = new Neo4jGraph(ctx);
        GraphLoader loader = new Neo4jGraphLoader(ctx);
        memGraph = new MemGraph(loader);
    }

    @Override
    public long createNode(NodeContext node) throws PMException {
        long id = dbGraph.createNode(node);
        node.id(id);
        memGraph.createNode(node);
        return id;
    }

    @Override
    public void updateNode(NodeContext node) throws PMException {
        dbGraph.updateNode(node);
        memGraph.updateNode(node);
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
    public HashSet<NodeContext> getNodes() {
        return memGraph.getNodes();
    }

    @Override
    public HashSet<Long> getPolicies() {
        return memGraph.getPolicies();
    }

    @Override
    public HashSet<NodeContext> getChildren(long nodeID) throws PMException {
        return memGraph.getChildren(nodeID);
    }

    @Override
    public HashSet<NodeContext> getParents(long nodeID) throws PMException {
        return memGraph.getParents(nodeID);
    }

    @Override
    public void assign(NodeContext childCtx, NodeContext parentCtx) throws PMException {
        dbGraph.assign(childCtx,parentCtx);
        memGraph.assign(childCtx, parentCtx);
    }

    @Override
    public void deassign(NodeContext childCtx, NodeContext parentCtx) throws PMException {
        dbGraph.deassign(childCtx, parentCtx);
        memGraph.deassign(childCtx, parentCtx);
    }

    @Override
    public void associate(NodeContext uaCtx, NodeContext targetCtx, HashSet<String> operations) throws PMException {
        dbGraph.associate(uaCtx, targetCtx, operations);
        memGraph.associate(uaCtx, targetCtx, operations);
    }

    @Override
    public void dissociate(NodeContext uaCtx, NodeContext targetCtx) throws PMException {
        dbGraph.dissociate(uaCtx, targetCtx);
        memGraph.dissociate(uaCtx, targetCtx);
    }

    @Override
    public HashMap<Long, HashSet<String>> getSourceAssociations(long sourceID) throws PMException {
        return memGraph.getSourceAssociations(sourceID);
    }

    @Override
    public HashMap<Long, HashSet<String>> getTargetAssociations(long targetID) throws PMException {
        return memGraph.getTargetAssociations(targetID);
    }

    @Override
    public HashSet<NodeContext> search(String name, String type, Map<String, String> properties) throws PMException {
        Search search = new MemGraphSearch(memGraph);
        return search.search(name, type, properties);
    }

    @Override
    public NodeContext getNode(long id) throws PMException {
        Search search = new MemGraphSearch(memGraph);
        return search.getNode(id);
    }
}
