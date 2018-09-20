package gov.nist.csd.pm.pep.requests;

public class PostSubjectToProhibitionRequest {
    long subjectID;
    String subjectType;
    public long getSubjectID(){
        return subjectID;
    }
    public String getSubjectType(){
        return subjectType;
    }
}
