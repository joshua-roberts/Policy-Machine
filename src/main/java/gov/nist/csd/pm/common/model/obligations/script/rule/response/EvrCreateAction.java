package gov.nist.csd.pm.common.model.obligations.script.rule.response;

import gov.nist.csd.pm.common.model.obligations.EvrEntity;
import gov.nist.csd.pm.common.model.obligations.EvrRule;
import gov.nist.csd.pm.common.model.obligations.script.rule.event.EvrTarget;

public class EvrCreateAction extends EvrAction {
    private EvrEntity entity;
    private EvrTarget target;
    private EvrRule rule;

    public EvrEntity getEntity() {
        return entity;
    }

    public void setEntity(EvrEntity entity) {
        this.entity = entity;
    }

    public boolean isNode() {
        return entity != null;
    }

    public EvrTarget getTarget() {
        return target;
    }

    public void setTarget(EvrTarget target) {
        this.target = target;
    }

    public EvrRule getRule() {
        return rule;
    }

    public void setRule(EvrRule rule) {
        this.rule = rule;
    }

    public boolean isRule() {
        return this.rule != null;
    }
}
