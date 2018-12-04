package gov.nist.csd.pm.common.model.prohibitions;

import java.io.Serializable;

/**
 * The subject of a prohibition.  The subject can be a User, User Attribute, or process (ProhibitionSubjectType) with an ID.
 */
public class ProhibitionSubject  implements Serializable {
    long                   subjectID;
    ProhibitionSubjectType subjectType;

    public ProhibitionSubject(){

    }

    public ProhibitionSubject(long subjectID, ProhibitionSubjectType subjectType) {
        this.subjectID = subjectID;
        this.subjectType = subjectType;
    }

    public long getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(long subjectID) {
        this.subjectID = subjectID;
    }

    public ProhibitionSubjectType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(ProhibitionSubjectType subjectType) {
        this.subjectType = subjectType;
    }
}
