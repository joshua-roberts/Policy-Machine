package gov.nist.csd.pm.pap.loader.graph;

import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.relationships.NGACAssignment;
import gov.nist.csd.pm.model.graph.relationships.NGACAssociation;

import java.util.HashSet;

/**
 * This implementation of the GraphLoader interface loads an empty graph. It will be used for
 * testing the in memory graph.
 */
public class DummyGraphLoader implements GraphLoader {

    @Override
    public HashSet<Node> getPolicies() {
        return new HashSet<>();
    }

    @Override
    public HashSet<Node> getNodes() {
        return new HashSet<>();
    }

    @Override
    public HashSet<NGACAssignment> getAssignments() {
        return new HashSet<>();
    }

    @Override
    public HashSet<NGACAssociation> getAssociations() {
        return new HashSet<>();
    }
}
