package gov.nist.csd.pm.common.model.obligations.script.rule.event.contexts;

public class EventContext {
    long userID;
    long processID;

    public EventContext(long userID, long processID) {
        this.userID = userID;
        this.processID = processID;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public long getProcessID() {
        return processID;
    }

    public void setProcessID(long processID) {
        this.processID = processID;
    }
}
