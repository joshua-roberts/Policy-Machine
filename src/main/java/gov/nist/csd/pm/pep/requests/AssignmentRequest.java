package gov.nist.csd.pm.pep.requests;

public class AssignmentRequest {
    private long childID;
    private String childType;
    private long parentID;
    private String parentType;

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

    public String getChildType() {
        return childType;
    }

    public void setChildType(String childType) {
        this.childType = childType;
    }

    public String getParentType() {
        return parentType;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }
}
