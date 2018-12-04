package gov.nist.csd.pm.epp;

import gov.nist.csd.pm.model.obligations.script.rule.event.contexts.EventContext;
import gov.nist.csd.pm.pdp.services.Service;

public class EventProcessor {

    private String sessionID;
    private long processID;

    public EventProcessor(String sessionID, long processID) {
        this.sessionID = sessionID;
        this.processID = processID;
    }

    public void processEventContext(EventContext ctx) {

    }
}
