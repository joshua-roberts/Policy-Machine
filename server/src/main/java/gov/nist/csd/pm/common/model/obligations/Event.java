package gov.nist.csd.pm.common.model.obligations;

import java.util.List;

public class Event {
    private Subject      subject;
    private PolicyClass  policyClass;
    private List<String> operations;
    private Target target;

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public PolicyClass getPolicyClass() {
        return policyClass;
    }

    public void setPolicyClass(PolicyClass policyClass) {
        this.policyClass = policyClass;
    }

    public List<String> getOperations() {
        return operations;
    }

    public void setOperations(List<String> operations) {
        this.operations = operations;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }
}
