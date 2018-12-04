package gov.nist.csd.pm.demos.nlpm;

import gov.nist.csd.pm.model.graph.OldNode;

import java.util.List;

public class PDLGrantStatement {
    List<OldNode> subjects;
    List<String>  operations;
    List<OldNode> targets;
    OldNode       pcNode;

    public PDLGrantStatement(List<OldNode> subjects, List<String> operations, List<OldNode> targets, OldNode pcNode) {
        this.subjects = subjects;
        this.operations = operations;
        this.targets = targets;
        this.pcNode = pcNode;
    }

    public List<OldNode> getSubjects() {
        return subjects;
    }

    public List<String> getOperations() {
        return operations;
    }

    public List<OldNode> getTargets() {
        return targets;
    }

    public OldNode getPcNode() {
        return pcNode;
    }
}
