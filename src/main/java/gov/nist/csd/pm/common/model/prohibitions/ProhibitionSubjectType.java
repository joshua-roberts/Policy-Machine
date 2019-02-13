package gov.nist.csd.pm.common.model.prohibitions;


import gov.nist.csd.pm.common.exceptions.Errors;
import gov.nist.csd.pm.common.exceptions.PMException;

import java.io.Serializable;

/**
 * The allowed types of subjects for a prohibition.
 *
 * UA = User Attribute
 * U = User
 * P = Process
 */
public enum ProhibitionSubjectType  implements Serializable {
    UA("UA"),
    U("U"),
    P("P");

    String value;
    ProhibitionSubjectType(String value){
        this.value = value;
    }
    public String toString(){
        return value;
    }

    /**
     * Convert a string to a ProhibitionSubjectType.
     * @param subjectType The string to convert.
     * @return The ProhibitionSubjectType that is equivalent to the provided String.
     * @throws PMException If the provided subject type is null.
     * @throws PMException If the provided subject type is invalid.
     */
    public static ProhibitionSubjectType toType(String subjectType) throws PMException {
        if(subjectType == null){
            throw new PMException(Errors.ERR_INVALID_PROHIBITION_SUBJECT_TYPE, "null is an invalid Prohibition subject type");
        }
        switch (subjectType.toUpperCase()){
            case "UA":
                return ProhibitionSubjectType.UA;
            case "U":
                return ProhibitionSubjectType.U;
            case "P":
                return ProhibitionSubjectType.P;
            default:
                throw new PMException(Errors.ERR_INVALID_PROHIBITION_SUBJECT_TYPE, String.format("%s is an invalid Prohibition subject type", subjectType));
        }
    }
}
