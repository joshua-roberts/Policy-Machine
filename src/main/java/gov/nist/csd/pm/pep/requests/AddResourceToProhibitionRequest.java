package gov.nist.csd.pm.pep.requests;

public class AddResourceToProhibitionRequest {
    long resourceID;
    boolean compliment;

    public long getResourceID() {
        return resourceID;
    }

    public boolean isCompliment() {
        return compliment;
    }
}
