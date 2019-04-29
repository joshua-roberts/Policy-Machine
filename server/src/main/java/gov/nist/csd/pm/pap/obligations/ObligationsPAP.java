package gov.nist.csd.pm.pap.obligations;

import gov.nist.csd.pm.common.model.obligations.Obligation;
import gov.nist.csd.pm.pip.obligations.MemObligations;
import gov.nist.csd.pm.pip.obligations.Neo4jObligations;
import gov.nist.csd.pm.pip.obligations.Obligations;

import java.util.List;

public class ObligationsPAP implements Obligations {

    private MemObligations   memObligations;
    private Neo4jObligations neo4jObligations;

    public ObligationsPAP(MemObligations memObligations, Neo4jObligations neo4jObligations) {
        this.memObligations = memObligations;
        this.neo4jObligations = neo4jObligations;
    }

    public MemObligations getMemObligations() {
        return new MemObligations();
    }

    public Neo4jObligations getNeo4jObligations() {
        return new Neo4jObligations();
    }

    @Override
    public void add(Obligation obligation) {

    }

    @Override
    public Obligation get(String label) {
        return null;
    }

    @Override
    public List<Obligation> getAll() {
        return null;
    }

    @Override
    public void update(String label, Obligation obligation) {

    }

    @Override
    public void delete(String label) {

    }
}
