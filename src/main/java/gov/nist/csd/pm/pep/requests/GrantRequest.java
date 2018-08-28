package gov.nist.csd.pm.pep.requests;

public     class GrantRequest {
    long subjectId;
    String[] operations;
    long[] targetIds;
    long pcId;

    public GrantRequest() {}

    public long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(long subjectId) {
        this.subjectId = subjectId;
    }

    public String[] getOperations() {
        return operations;
    }

    public void setOperations(String[] operations) {
        this.operations = operations;
    }

    public long[] getTargetIds() {
        return targetIds;
    }

    public void setTargetIds(long[] targetIds) {
        this.targetIds = targetIds;
    }

    public long getPcId() {
        return pcId;
    }

    public void setPcId(long pcId) {
        this.pcId = pcId;
    }
}