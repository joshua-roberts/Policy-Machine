package gov.nist.csd.pm.pap.loader.graph;

import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.common.model.graph.relationships.Assignment;
import gov.nist.csd.pm.common.model.graph.relationships.Association;

import java.util.HashSet;

/**
 * This implementation of the GraphLoader interface loads an empty graph. It will be used for
 * testing the in memory graph.
 */
public class DummyGraphLoader implements GraphLoader {

    @Override
    public HashSet<Long> getPolicies() {
        return new HashSet<>();
    }

    @Override
    public HashSet<NodeContext> getNodes() {
        return new HashSet<>();
    }

    @Override
    public HashSet<Assignment> getAssignments() {
        return new HashSet<>();
    }

    @Override
    public HashSet<Association> getAssociations() {
        return new HashSet<>();
    }
}
