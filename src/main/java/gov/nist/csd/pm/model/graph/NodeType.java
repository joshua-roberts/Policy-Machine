package gov.nist.csd.pm.model.graph;

import gov.nist.csd.pm.model.exceptions.InvalidNodeTypeException;

import java.io.Serializable;

public enum NodeType implements Serializable {
    CONNECTOR("C"),
    OBJECT_ATTRIBUTE("OA"),
    USER_ATTRIBUTE("UA"),
    USER("U"),
    OBJECT("O"),
    POLICY_CLASS("PC"),
    PROHIBITION("D"),
    OPERATION_SET("OS"),
    SESSION("S");

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
                return NodeType.CONNECTOR;
            case "OA":
                return NodeType.OBJECT_ATTRIBUTE;
            case "UA":
                return NodeType.USER_ATTRIBUTE;
            case "U":
                return NodeType.USER;
            case "O":
                return NodeType.OBJECT;
            case "PC":
                return NodeType.POLICY_CLASS;
            case "D":
                return NodeType.PROHIBITION;
            case "OS":
                return NodeType.OPERATION_SET;
            case "S":
                return NodeType.SESSION;
            default:
                throw new InvalidNodeTypeException(type);
        }
    }

    public static NodeType toNodeType(int typeId) throws InvalidNodeTypeException {
        if(typeId <= 0 || typeId > 7){
            throw new InvalidNodeTypeException(typeId);
        }

        switch(typeId){
            case 1:
                return NodeType.CONNECTOR;
            case 2:
                return NodeType.POLICY_CLASS;
            case 3:
                return NodeType.USER_ATTRIBUTE;
            case 4:
                return NodeType.USER;
            case 5:
                return NodeType.OBJECT_ATTRIBUTE;
            case 6:
                return NodeType.OBJECT;
            case 7:
                return NodeType.OPERATION_SET;
            case 8:
                return NodeType.SESSION;
            default:
                throw new InvalidNodeTypeException(typeId);
        }
    }

    public static int toNodeTypeId(String type) throws InvalidNodeTypeException {
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
