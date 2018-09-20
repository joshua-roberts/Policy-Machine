package gov.nist.csd.pm.demos.nlpm;

import gov.nist.csd.pm.model.graph.Node;

import java.util.List;

public class PDLGrantStatement {
    List<Node> subjects;
    List<String> operations;
    List<Node> targets;
    Node pcNode;

    public PDLGrantStatement(List<Node> subjects, List<String> operations, List<Node> targets, Node pcNode) {
        this.subjects = subjects;
        this.operations = operations;
        this.targets = targets;
        this.pcNode = pcNode;
    }

    public List<Node> getSubjects() {
        return subjects;
    }

    public List<String> getOperations() {
        return operations;
    }

    public List<Node> getTargets() {
        return targets;
    }

    public Node getPcNode() {
        return pcNode;
    }
}
