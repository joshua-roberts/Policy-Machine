package gov.nist.csd.pm.common.model.obligations.script.rule.response;

import gov.nist.csd.pm.common.model.obligations.EvrEntity;

public class EvrAssignAction extends EvrAction {
    private EvrEntity child;
    private EvrEntity parent;

    public EvrEntity getChild() {
        return child;
    }

    public void setChild(EvrEntity child) {
        this.child = child;
    }

    public EvrEntity getParent() {
        return parent;
    }

    public void setParent(EvrEntity parent) {
        this.parent = parent;
    }
}
