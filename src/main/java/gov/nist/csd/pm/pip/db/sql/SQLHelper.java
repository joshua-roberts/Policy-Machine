package gov.nist.csd.pm.pip.db.sql;

import java.util.HashSet;

/**
 * Helper methods for SQL
 */
public class SQLHelper {

    /**
     * Given a set of Strings, return a single string with values separated by the given separator.
     * @param inValue The HashSet to convert to a string.
     * @param separator The String to use as a separator between the values.
     * @return A string of the given HashSet's values separated by the provided separator.
     */
    public static String setToString(HashSet<String> inValue, String separator){
        String values = "";
        for(String value : inValue) {
            values += value + separator;
        }
        values = values.substring(0, values.length()-1);
        return values;
    }

    /**
     * Given an array of Strings, return a single string with values separated by the given separator.
     * @param inValue The array to convert to a string.
     * @param separator The String to use as a separator between the values.
     * @return A string of the given array's values separated by the provided separator.
     */
    public static String arrayToString(String[] inValue, String separator){
        String values = "";
        for(String value : inValue) {
            values += value + separator;
        }
        values = values.substring(0, values.length()-1);
        return values;
    }
}
