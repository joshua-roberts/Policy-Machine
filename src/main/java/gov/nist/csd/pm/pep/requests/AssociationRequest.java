package gov.nist.csd.pm.pep.requests;

import java.util.HashSet;

public class AssociationRequest {
    long            sourceID;
    long            targetID;
    HashSet<String> operations;

    public long getSourceID() {
        return sourceID;
    }

    public void setSourceID(long sourceID) {
        this.sourceID = sourceID;
    }

    public long getTargetID() {
        return targetID;
    }

    public void setTargetID(long targetID) {
        this.targetID = targetID;
    }

    public HashSet<String> getOperations() {
        return operations;
    }

    public void setOperations(HashSet<String> operations) {
        this.operations = operations;
    }
}
