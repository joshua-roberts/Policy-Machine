package gov.nist.csd.pm.pap.loader.prohibitions;

import gov.nist.csd.pm.model.prohibitions.Prohibition;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ProhibitionsLoader that loads an empty set of prohibitions.
 */
public class DummyProhibitionsLoader implements ProhibitionsLoader {
    @Override
    public List<Prohibition> loadProhibitions() {
        return new ArrayList<>();
    }
}
