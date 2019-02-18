package gov.nist.csd.pm.common.model.prohibitions;


import gov.nist.csd.pm.common.exceptions.Errors;
import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.exceptions.PMProhibitionException;

import java.io.Serializable;

/**
 * The allowed types of subjects for a prohibition.
 *
 * UA = user attribute
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
     * @return the ProhibitionSubjectType that is equivalent to the provided String.
     * @throws PMProhibitionException if the provided subject type is null.
     * @throws PMProhibitionException if the provided subject type is invalid.
     */
    public static ProhibitionSubjectType toType(String subjectType) throws PMProhibitionException {
        if(subjectType == null){
            throw new PMProhibitionException("null is an invalid Prohibition subject type");
        }
        switch (subjectType.toUpperCase()){
            case "UA":
                return ProhibitionSubjectType.UA;
            case "U":
                return ProhibitionSubjectType.U;
            case "P":
                return ProhibitionSubjectType.P;
            default:
                throw new PMProhibitionException(String.format("%s is an invalid Prohibition subject type", subjectType));
        }
    }
}
