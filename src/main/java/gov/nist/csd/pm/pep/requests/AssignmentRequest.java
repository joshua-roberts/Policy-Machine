package gov.nist.csd.pm.pep.requests;

public class AssignmentRequest {
    private long childID;
    private long parentID;

    public long getChildID() {
        return childID;
    }

    public void setChildID(long childID) {
        this.childID = childID;
    }

    public long getParentID() {
        return parentID;
    }

    public void setParentID(long parentID) {
        this.parentID = parentID;
    }
}
