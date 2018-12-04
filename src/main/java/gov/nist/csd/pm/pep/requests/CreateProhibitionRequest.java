package gov.nist.csd.pm.pep.requests;

import gov.nist.csd.pm.common.model.prohibitions.ProhibitionNode;
import gov.nist.csd.pm.common.model.prohibitions.ProhibitionSubject;

import java.util.HashSet;
import java.util.List;

public class CreateProhibitionRequest {
    public String                name;
    public boolean               intersection;
    public HashSet<String>       operations;
    public List<ProhibitionNode> nodes;
    public ProhibitionSubject    subject;

    public String getName() {
        return name;
    }

    public boolean isIntersection() {
        return intersection;
    }

    public HashSet<String> getOperations() {
        return operations;
    }

    public List<ProhibitionNode> getNodes() {
        return nodes;
    }

    public ProhibitionSubject getSubject() {
        return subject;
    }
}
