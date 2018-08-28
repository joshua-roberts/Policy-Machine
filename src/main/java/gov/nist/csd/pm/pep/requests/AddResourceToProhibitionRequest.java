package gov.nist.csd.pm.pep.requests;

public class AddResourceToProhibitionRequest {
    long resourceId;
    boolean compliment;

    public long getResourceId() {
        return resourceId;
    }

    public boolean isCompliment() {
        return compliment;
    }
}
