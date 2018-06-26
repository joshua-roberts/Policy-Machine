package gov.nist.policyserver.obligations.model.script.rule.event;

import java.util.HashSet;
import java.util.List;

public class EvrOpSpec {
    private HashSet<String> ops;

    public EvrOpSpec(HashSet<String> ops) {
        this.ops = ops;
    }

    public HashSet<String> getOps() {
        return ops;
    }

    public void setOps(HashSet<String> ops) {
        this.ops = ops;
    }

    public boolean isAny() {
        return ops.isEmpty();
    }
}
