package gov.nist.csd.pm.common.model.obligations.script.rule.response;

import gov.nist.csd.pm.common.model.obligations.script.rule.event.EvrOpertations;
import gov.nist.csd.pm.common.model.obligations.script.rule.event.EvrSubject;
import gov.nist.csd.pm.common.model.obligations.script.rule.event.EvrTarget;

public class EvrDenyAction extends EvrAction {
    private EvrSubject     subject;
    private EvrOpertations operations;
    private EvrTarget      target;

    public EvrSubject getSubject() {
        return subject;
    }

    public void setSubject(EvrSubject subject) {
        this.subject = subject;
    }

    public EvrOpertations getOperations() {
        return operations;
    }

    public void setEvrOperations(EvrOpertations operations) {
        this.operations = operations;
    }

    public EvrTarget getTarget() {
        return target;
    }

    public void setTarget(EvrTarget target) {
        this.target = target;
    }
}
