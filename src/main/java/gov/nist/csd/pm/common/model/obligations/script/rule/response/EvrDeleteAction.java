package gov.nist.csd.pm.common.model.obligations.script.rule.response;

import gov.nist.csd.pm.common.model.obligations.EvrRule;

public class EvrDeleteAction extends EvrAction {
    private EvrAction evrAction;
    private EvrRule evrRule;

    public EvrAction getEvrAction() {
        return evrAction;
    }

    public void setEvrAction(EvrAction evrAction) {
        this.evrAction = evrAction;
    }

    public EvrRule getEvrRule() {
        return evrRule;
    }

    public void setEvrRule(EvrRule evrRule) {
        this.evrRule = evrRule;
    }
}
