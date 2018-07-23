package gov.nist.policyserver.model.prohibitions;

import java.io.Serializable;

public class ProhibitionSubject  implements Serializable {
    long id;
    ProhibitionSubjectType type;

    public ProhibitionSubject(){

    }

    public ProhibitionSubject(long subjectId, ProhibitionSubjectType subjectType) {
        this.id = subjectId;
        this.type = subjectType;
    }

    public long getSubjectId() {
        return id;
    }

    public void setSubjectId(long subjectId) {
        this.id = subjectId;
    }

    public ProhibitionSubjectType getSubjectType() {
        return type;
    }

    public void setSubjectType(ProhibitionSubjectType subjectType) {
        this.type = subjectType;
    }
}
