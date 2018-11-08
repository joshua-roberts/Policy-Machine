package gov.nist.csd.pm.model.graph.nodes;

import gov.nist.csd.pm.model.exceptions.InvalidNodeTypeException;

import java.io.Serializable;

/**
 * Allowed types of nodes in an NGAC Graph
 *
 * OA = Object Attribute
 * UA = User Attribute
 * U = User
 * O = Object
 * PC = Policy Class
 * OS = Operation Set
 */
public enum NodeType implements Serializable {
    OA("OA"),
    UA("UA"),
    U("U"),
    O("O"),
    PC("PC"),
    OS("OS");

    //NodeType ids
    public static final int PC_ID = 2;
    public static final int UA_ID = 3;
    public static final int U_ID  = 4;
    public static final int OA_ID = 5;
    public static final int O_ID  = 6;
    public static final int OS_ID = 7;

    private String label;
    NodeType(String label){
        this.label = label;
    }
    public String toString(){
        return label;
    }

    /**
     * Given a string, return the matching NodeType. If the type is null or not one of the types listed above,
     * an InvalidNodeTypeExceptino will be thrown.
     * @param type The String type to convert to a NodeType.
     * @return The equivalent NodeType of the given String.
     * @throws InvalidNodeTypeException When an invalid or null type is provided.
     */
    public static NodeType toNodeType(String type) throws InvalidNodeTypeException {
        if(type == null){
            throw new InvalidNodeTypeException(null);
        }
        switch (type.toUpperCase()){
            case "OA":
                return NodeType.OA;
            case "UA":
                return NodeType.UA;
            case "U":
                return NodeType.U;
            case "O":
                return NodeType.O;
            case "PC":
                return NodeType.PC;
            case "OS":
                return NodeType.OS;
            default:
                throw new InvalidNodeTypeException(type);
        }
    }

    public static NodeType toNodeType(int typeID) throws InvalidNodeTypeException {
        if(typeID <= 0 || typeID > 7){
            throw new InvalidNodeTypeException(typeID);
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
                throw new InvalidNodeTypeException(typeID);
        }
    }

    public static int toNodeTypeID(String type) throws InvalidNodeTypeException {
        if(type == null){
            throw new InvalidNodeTypeException(type);
        }

        switch (type.toUpperCase()){
            case "OA":
                return NodeType.OA_ID;
            case "UA":
                return NodeType.UA_ID;
            case "U":
                return NodeType.U_ID;
            case "O":
                return NodeType.O_ID;
            case "PC":
                return NodeType.PC_ID;
            case "OS":
                return NodeType.OS_ID;
            default:
                throw new InvalidNodeTypeException(type);
        }
    }
}
