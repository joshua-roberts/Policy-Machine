package gov.nist.csd.pm.pdp.engine;

import java.util.HashSet;

public class Decision {
    private long            userID;
    private long            targetID;
    private HashSet<String> operations;

    public Decision(long userID, long targetID, HashSet<String> operations) {
        this.userID = userID;
        this.targetID = targetID;
        this.operations = operations;
    }

    public long getUserID() {
        return userID;
    }

    public long getTargetID() {
        return targetID;
    }

    public HashSet<String> getOperations() {
        return operations;
    }
}
