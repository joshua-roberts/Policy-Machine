package gov.nist.policyserver.analytics;

import gov.nist.policyserver.model.graph.nodes.Node;

import java.util.HashSet;

public class PmAnalyticsEntry {
    Node            target;
    HashSet<String> operations;

    public PmAnalyticsEntry(){
        this.operations = new HashSet<>();
    }

    public PmAnalyticsEntry(Node node) {
        this.target = node;
        this.operations = new HashSet<>();
    }

    public PmAnalyticsEntry(Node target, HashSet<String> operations) {
        this.target = target;
        this.operations = operations;
    }

    public Node getTarget() {
        return target;
    }

    public void setTarget(Node target) {
        this.target = target;
    }

    public HashSet<String> getOperations() {
        return operations;
    }

    public void setOperations(HashSet<String> operations) {
        this.operations = operations;
    }
}
