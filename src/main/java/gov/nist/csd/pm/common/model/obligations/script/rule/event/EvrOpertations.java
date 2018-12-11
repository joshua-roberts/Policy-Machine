package gov.nist.csd.pm.common.model.obligations.script.rule.event;

import java.util.HashSet;
import java.util.List;

public class EvrOpertations {
    private HashSet<String> ops;

    public EvrOpertations(HashSet<String> ops) {
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
