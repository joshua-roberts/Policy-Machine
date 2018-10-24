package gov.nist.csd.pm.pip.loader;

import gov.nist.csd.pm.model.graph.relationships.NGACAssignment;
import gov.nist.csd.pm.model.graph.relationships.NGACAssociation;

import java.util.HashSet;

/**
 * This implementation of the Loader interface loads an empty graph. It will be used for
 * testing the in memory graph.
 */
public class DummyLoader implements Loader {

    @Override
    public HashSet<Long> getPolicies() throws LoaderException {
        return new HashSet<>();
    }

    @Override
    public HashSet<Long> getNodes() throws LoaderException {
        return new HashSet<>();
    }

    @Override
    public HashSet<NGACAssignment> getAssignments() throws LoaderException {
        return new HashSet<>();
    }

    @Override
    public HashSet<NGACAssociation> getAssociations() throws LoaderException {
        return new HashSet<>();
    }
}
