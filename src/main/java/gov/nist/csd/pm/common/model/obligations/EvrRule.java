package gov.nist.csd.pm.common.model.obligations;

import gov.nist.csd.pm.common.model.obligations.script.rule.event.time.EvrEvent;
import gov.nist.csd.pm.common.model.obligations.script.rule.response.EvrResponse;

public class EvrRule {
    private String   label;
    private EvrEvent event;
    private EvrResponse response;

    public EvrRule(EvrEvent event, EvrResponse response) {
        this.event = event;
        this.response = response;
    }

    public EvrRule() {}

    public EvrRule(String ruleLabel, EvrEvent evrEvent, EvrResponse evrResponse) {
        this.label = ruleLabel;
        this.event = evrEvent;
        this.response = evrResponse;
    }

    public EvrEvent getEvent() {
        return event;
    }

    public void setEvent(EvrEvent event) {
        this.event = event;
    }

    public EvrResponse getResponse() {
        return response;
    }

    public void setResponse(EvrResponse response) {
        this.response = response;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
