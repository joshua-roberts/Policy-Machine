package gov.nist.csd.pm.epp;

import gov.nist.csd.pm.common.model.obligations.Obligation;

public interface EventMatcher {
    boolean matches(Obligation obligation);
}
