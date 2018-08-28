package gov.nist.csd.pm.pep.requests;

import gov.nist.csd.pm.model.prohibitions.ProhibitionResource;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubject;

public class CreateProhibitionRequest {
    public String                name;
    public boolean               intersection;
    public String[]              operations;
    public ProhibitionResource[] resources;
    public ProhibitionSubject    subject;

    public String getName() {
        return name;
    }

    public boolean isIntersection() {
        return intersection;
    }

    public String[] getOperations() {
        return operations;
    }

    public ProhibitionResource[] getResources() {
        return resources;
    }

    public ProhibitionSubject getSubject() {
        return subject;
    }
}
