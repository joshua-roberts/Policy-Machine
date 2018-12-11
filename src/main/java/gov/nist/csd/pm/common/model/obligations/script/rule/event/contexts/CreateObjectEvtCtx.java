package gov.nist.csd.pm.common.model.obligations.script.rule.event.contexts;

public class CreateObjectEvtCtx extends EventContext{
    long baseID;

    public CreateObjectEvtCtx(long userID, long processID, long baseID) {
        super(userID, processID);
        this.baseID = baseID;
    }

    public long getBaseID() {
        return baseID;
    }

    public void setBaseID(long baseID) {
        this.baseID = baseID;
    }
}
