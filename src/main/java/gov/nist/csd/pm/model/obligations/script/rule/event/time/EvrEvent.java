package gov.nist.csd.pm.model.obligations.script.rule.event.time;

import gov.nist.csd.pm.model.obligations.script.rule.event.EvrOpertations;
import gov.nist.csd.pm.model.obligations.script.rule.event.EvrPolicies;
import gov.nist.csd.pm.model.obligations.script.rule.event.EvrSubject;
import gov.nist.csd.pm.model.obligations.script.rule.event.EvrTarget;

public class EvrEvent {
    EvrSubject     subject;
    EvrPolicies    policies;
    EvrOpertations operations;
    EvrTarget      target;
    EvrTime        time;

    public EvrSubject getSubject() {
        return subject;
    }

    public void setSubject(EvrSubject subject) {
        this.subject = subject;
    }

    public EvrPolicies getPolicies() {
        return policies;
    }

    public void setEvrPolicies(EvrPolicies policies) {
        this.policies = policies;
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

    public void setTime(EvrTime time) {
        this.time = time;
    }

    public EvrTime getTime() {
        return time;
    }

    public boolean isTime() {
        return time != null;
    }

    public boolean equals(Object o) {
        if(!(o instanceof EvrEvent)) {
            return false;
        }

        EvrEvent event = (EvrEvent) o;

        //if one is time and the other not time return false
        if(this.isTime() != event.isTime()) {
            return false;
        } else if(this.getTime().equals(event.getTime())) {
            return true;
        }

        //if it gets to this point, it is not time
        return this.getSubject().equals(event.getSubject()) &&
                this.getPolicies().equals(event.getPolicies()) &&
                this.getOperations().equals(event.getOperations()) &&
                this.getTarget().equals(event.getTarget());
    }
}
