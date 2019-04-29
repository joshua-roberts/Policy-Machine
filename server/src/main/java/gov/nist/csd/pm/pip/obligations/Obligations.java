package gov.nist.csd.pm.pip.obligations;

import gov.nist.csd.pm.common.model.obligations.Obligation;

import java.util.List;

public interface Obligations {
    void add(Obligation obligation);

    Obligation get(String label);

    List<Obligation> getAll();

    void update(String label, Obligation obligation);

    void delete(String label);
}
