package gov.nist.csd.pm.pap.db.sql;

import gov.nist.csd.pm.common.model.graph.nodes.NodeType;

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

    //NodeType ids
    public static final int PC_ID = 2;
    public static final int UA_ID = 3;
    public static final int U_ID  = 4;
    public static final int OA_ID = 5;
    public static final int O_ID  = 6;
    public static final int OS_ID = 7;

    public static NodeType toNodeType(int typeID) {
        if(typeID <= 0 || typeID > 7){
            throw new IllegalArgumentException(typeID + " is not a valid NGAC NodeType ID");
        }

        switch(typeID){
            case 2:
                return NodeType.PC;
            case 3:
                return NodeType.UA;
            case 4:
                return NodeType.U;
            case 5:
                return NodeType.OA;
            case 6:
                return NodeType.O;
            case 7:
                return NodeType.OS;
            default:
                throw new IllegalArgumentException(typeID + " is not a valid NGAC NodeType ID");
        }
    }

    public static int toNodeTypeID(String type) {
        if(type == null){
            throw new IllegalArgumentException("null is not a valid NGAC NodeType");
        }

        switch (type.toUpperCase()){
            case "OA":
                return OA_ID;
            case "UA":
                return UA_ID;
            case "U":
                return U_ID;
            case "O":
                return O_ID;
            case "PC":
                return PC_ID;
            case "OS":
                return OS_ID;
            default:
                throw new IllegalArgumentException(type + " is not a valid NGAC NodeType");
        }
    }
}
