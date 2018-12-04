package gov.nist.csd.pm.demos.egrant;

public     class GrantRequest {
    long subjectID;
    String[] operations;
    long[] targetIDs;
    long pcID;

    public GrantRequest() {}

    public long getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(long subjectID) {
        this.subjectID = subjectID;
    }

    public String[] getOperations() {
        return operations;
    }

    public void setOperations(String[] operations) {
        this.operations = operations;
    }

    public long[] getTargetIDs() {
        return targetIDs;
    }

    public void setTargetIDs(long[] targetIDs) {
        this.targetIDs = targetIDs;
    }

    public long getPcID() {
        return pcID;
    }

    public void setPcID(long pcID) {
        this.pcID = pcID;
    }
}