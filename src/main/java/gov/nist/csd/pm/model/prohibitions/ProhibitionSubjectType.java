package gov.nist.csd.pm.model.prohibitions;


import gov.nist.csd.pm.model.exceptions.InvalidProhibitionSubjectTypeException;

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
     * @throws InvalidProhibitionSubjectTypeException When the provided String is not a valid type ir is null.
     */
    public static ProhibitionSubjectType toType(String subjectType) throws InvalidProhibitionSubjectTypeException {
        if(subjectType == null){
            throw new InvalidProhibitionSubjectTypeException(null);
        }
        switch (subjectType.toUpperCase()){
            case "UA":
                return ProhibitionSubjectType.UA;
            case "U":
                return ProhibitionSubjectType.U;
            case "P":
                return ProhibitionSubjectType.P;
            default:
                throw new InvalidProhibitionSubjectTypeException(subjectType);
        }
    }
}
