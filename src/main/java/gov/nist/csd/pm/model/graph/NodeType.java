package gov.nist.csd.pm.model.graph;

import gov.nist.csd.pm.model.exceptions.InvalidNodeTypeException;

import java.io.Serializable;

public enum NodeType implements Serializable {
    C("C"),
    OA("OA"),
    UA("UA"),
    U("U"),
    O("O"),
    PC("PC"),
    P("P"),
    OS("OS");

    //Node_Type ids
    public static final int C_ID  = 1;
    public static final int PC_ID = 2;
    public static final int UA_ID = 3;
    public static final int U_ID  = 4;
    public static final int OA_ID = 5;
    public static final int O_ID  = 6;
    public static final int OS_ID = 7;
    public static final int S_ID  = 8;

    private String label;
    NodeType(String label){
        this.label = label;
    }
    public String toString(){
        return label;
    }

    public static NodeType toNodeType(String type) throws InvalidNodeTypeException {
        if(type == null){
            throw new InvalidNodeTypeException(type);
        }
        switch (type.toUpperCase()){
            case "C":
                return NodeType.C;
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
            case "D":
                return NodeType.P;
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
            case 1:
                return NodeType.C;
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
            case "C":
                return NodeType.C_ID;
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
            case "S":
                return NodeType.S_ID;
            default:
                throw new InvalidNodeTypeException(type);
        }
    }
}
