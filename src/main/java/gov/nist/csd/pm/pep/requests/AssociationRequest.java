package gov.nist.csd.pm.pep.requests;

import java.util.HashSet;

public class AssociationRequest {
    long            uaID;
    long            targetID;
    HashSet<String> ops;

    public long getUaID() {
        return uaID;
    }

    public void setUaID(long uaID) {
        this.uaID = uaID;
    }

    public long getTargetID() {
        return targetID;
    }

    public void setTargetID(long targetID) {
        this.targetID = targetID;
    }

    public HashSet<String> getOps() {
        return ops;
    }

    public void setOps(HashSet<String> ops) {
        this.ops = ops;
    }
}
