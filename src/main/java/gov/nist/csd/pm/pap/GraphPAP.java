package gov.nist.csd.pm.pap;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.Graph;
import gov.nist.csd.pm.graph.MemGraph;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.relationships.Assignment;
import gov.nist.csd.pm.graph.model.relationships.Association;
import gov.nist.csd.pm.pip.graph.Neo4jGraph;
import gov.nist.csd.pm.pip.loader.graph.GraphLoader;
import gov.nist.csd.pm.pip.loader.graph.Neo4jGraphLoader;
import gov.nist.csd.pm.pip.db.DatabaseContext;
import gov.nist.csd.pm.pip.search.MemGraphSearch;
import gov.nist.csd.pm.pip.search.Search;

import java.util.*;

public class GraphPAP implements Graph {

    private Neo4jGraph dbGraph;
    private MemGraph memGraph;

    public GraphPAP(DatabaseContext ctx) throws PMException {
        dbGraph = new Neo4jGraph(ctx);

        GraphLoader loader = new Neo4jGraphLoader(ctx);
        memGraph = new MemGraph();

        Set<Node> nodes = loader.getNodes();
        for(Node node : nodes) {
            memGraph.createNode(node);
        }

        Set<Assignment> assignments = loader.getAssignments();
        for(Assignment assignment : assignments) {
            long childID = assignment.getSourceID();
            long parentID = assignment.getTargetID();
            memGraph.assign(memGraph.getNode(childID), memGraph.getNode(parentID));
        }

        Set<Association> associations = loader.getAssociations();
        for(Association association : associations) {
            long uaID = association.getSourceID();
            long targetID = association.getTargetID();
            Set<String> operations = association.getOperations();
            memGraph.associate(memGraph.getNode(uaID), memGraph.getNode(targetID), operations);
        }
    }

    @Override
    public long createNode(Node node) throws PMException {
        long id = dbGraph.createNode(node);
        node.id(id);
        memGraph.createNode(node);
        return id;
    }

    @Override
    public void updateNode(Node node) throws PMException {
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
    public void assign(Node childCtx, Node parentCtx) throws PMException {
        dbGraph.assign(childCtx,parentCtx);
        memGraph.assign(childCtx, parentCtx);
    }

    @Override
    public void deassign(Node childCtx, Node parentCtx) throws PMException {
        dbGraph.deassign(childCtx, parentCtx);
        memGraph.deassign(childCtx, parentCtx);
    }

    @Override
    public void associate(Node uaCtx, Node targetCtx, Set<String> operations) throws PMException {
        dbGraph.associate(uaCtx, targetCtx, operations);
        memGraph.associate(uaCtx, targetCtx, operations);
    }

    @Override
    public void dissociate(Node uaCtx, Node targetCtx) throws PMException {
        dbGraph.dissociate(uaCtx, targetCtx);
        memGraph.dissociate(uaCtx, targetCtx);
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
    public Set<Node> search(String name, String type, Map<String, String> properties) throws PMDBException, PMGraphException {
        Search search = new MemGraphSearch(memGraph);
        return search.search(name, type, properties);
    }

    @Override
    public Node getNode(long id) throws PMException {
        Search search = new MemGraphSearch(memGraph);
        return search.getNode(id);
    }

    public void reset() throws PMDBException {
        Collection<Node> nodes = memGraph.getNodes();
        for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
            Node n = iterator.next();
            dbGraph.deleteNode(n.getID());
            iterator.remove();
        }
    }
}
