package gov.nist.csd.pm.pep.requests;

import gov.nist.csd.pm.prohibitions.model.Prohibition;

import java.util.List;
import java.util.Set;

public class CreateProhibitionRequest {
    private String                 name;
    private boolean                intersection;
    private Set<String>        operations;
    private List<Prohibition.Node> nodes;
    private Prohibition.Subject     subject;

    public String getName() {
        return name;
    }

    public boolean isIntersection() {
        return intersection;
    }

    public Set<String> getOperations() {
        return operations;
    }

    public List<Prohibition.Node> getNodes() {
        return nodes;
    }

    public Prohibition.Subject getSubject() {
        return subject;
    }
}
